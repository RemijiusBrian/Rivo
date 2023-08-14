package dev.ridill.mym.welcomeFlow.presentation

import android.content.Intent
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.ridill.mym.R
import dev.ridill.mym.core.data.preferences.PreferencesManager
import dev.ridill.mym.core.domain.service.AppDistributionService
import dev.ridill.mym.core.domain.service.GoogleSignInService
import dev.ridill.mym.core.domain.util.BuildUtil
import dev.ridill.mym.core.domain.util.EventBus
import dev.ridill.mym.core.domain.util.Zero
import dev.ridill.mym.core.ui.util.UiText
import dev.ridill.mym.settings.domain.backup.BackupWorkManager
import dev.ridill.mym.welcomeFlow.domain.model.WelcomeFlowStop
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WelcomeFlowViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val preferenceManager: PreferencesManager,
    private val appDistributionService: AppDistributionService,
    private val eventBus: EventBus<WelcomeFlowEvent>,
    private val signInService: GoogleSignInService,
    private val backupWorkManager: BackupWorkManager
) : ViewModel(), WelcomeFlowActions {

    val currentFlowStop = savedStateHandle.getStateFlow(FLOW_STOP, WelcomeFlowStop.WELCOME)
    val incomeInput = savedStateHandle.getStateFlow(INCOME_INPUT, "")
    val showNotificationRationale = savedStateHandle
        .getStateFlow(SHOW_NOTIFICATION_RATIONALE, false)
    val showNextButton = currentFlowStop.map { stop ->
        stop != WelcomeFlowStop.RESTORE_DATA
    }.distinctUntilChanged()

    val events = eventBus.eventFlow

    init {
        observeRestoreWork()
    }

    override fun onNextClick() {
        when (currentFlowStop.value) {
            WelcomeFlowStop.WELCOME -> {
                savedStateHandle[FLOW_STOP] = if (BuildUtil.isBuildInternalRelease())
                    WelcomeFlowStop.ENABLE_TESTING_FEATURES
                else
                    WelcomeFlowStop.RESTORE_DATA
            }

            WelcomeFlowStop.ENABLE_TESTING_FEATURES -> {
                signInTesterAndContinue()
            }

            WelcomeFlowStop.RESTORE_DATA -> {}

            WelcomeFlowStop.INCOME_SET -> {
                updateIncomeAndContinue()
            }
        }
    }

    override fun onCheckForBackupClick() {
        viewModelScope.launch {
            val intent = signInService.getSignInIntent()
            eventBus.send(WelcomeFlowEvent.LaunchGoogleSignIn(intent))
        }
    }

    override fun onSkipDataRestore() {
        savedStateHandle[FLOW_STOP] = WelcomeFlowStop.INCOME_SET
    }

    fun onGoogleSignInResult(intent: Intent?) = viewModelScope.launch {
        val account = signInService.getAccountFromIntent(intent)
        if (account == null) {
            eventBus.send(
                WelcomeFlowEvent.ShowUiMessage(
                    UiText.StringResource(
                        R.string.error_sign_in_failed,
                        true
                    )
                )
            )
            return@launch
        }

        restoreDataIfBackupExists()
    }

    private fun restoreDataIfBackupExists() {
        backupWorkManager.runRestoreWorkerNow()
    }

    private fun observeRestoreWork() = viewModelScope.launch {
        /*backupWorkManager.getRestoreWorkInfoLiveData().asFlow().collectLatest { info ->
            log { "Restore Work - ${info.state}" }
            when (info.state) {
                WorkInfo.State.ENQUEUED -> {}
                WorkInfo.State.RUNNING -> {}
                WorkInfo.State.SUCCEEDED -> {
                    preferenceManager.concludeWelcomeFlow()
                    eventBus.send(WelcomeFlowEvent.RestartApplication)
                }

                WorkInfo.State.FAILED -> {
                    eventBus.send(WelcomeFlowEvent.ShowUiMessage(UiText.StringResource(R.string.error_app_data_restore_failed)))
                }

                WorkInfo.State.BLOCKED -> {}
                WorkInfo.State.CANCELLED -> {}
            }
        }*/
    }

    private fun signInTesterAndContinue() = viewModelScope.launch {
        val message = appDistributionService.signInTester()
        if (message.isErrorText) {
            eventBus.send(WelcomeFlowEvent.ShowUiMessage(message))
            return@launch
        }

        savedStateHandle[FLOW_STOP] = WelcomeFlowStop.INCOME_SET
    }

    private fun updateIncomeAndContinue() = viewModelScope.launch {
        val limitValue = incomeInput.value.toLongOrNull() ?: -1L
        if (limitValue <= Long.Zero) {
            eventBus.send(
                WelcomeFlowEvent.ShowUiMessage(
                    UiText.StringResource(R.string.error_invalid_amount, true)
                )
            )
            return@launch
        }
        preferenceManager.updateMonthlyLimit(limitValue)
        if (BuildUtil.isNotificationRuntimePermissionNeeded()) {
            savedStateHandle[SHOW_NOTIFICATION_RATIONALE] = true
        } else {
            concludeWelcomeFlow()
        }
    }

    private fun concludeWelcomeFlow() = viewModelScope.launch {
        preferenceManager.concludeWelcomeFlow()
        eventBus.send(WelcomeFlowEvent.WelcomeFlowConcluded)
    }

    override fun onIncomeInputChange(value: String) {
        savedStateHandle[INCOME_INPUT] = value
    }

    override fun onNotificationRationaleDismiss() {
        savedStateHandle[SHOW_NOTIFICATION_RATIONALE] = false
        concludeWelcomeFlow()
    }

    override fun onNotificationRationaleAgree() {
        viewModelScope.launch {
            savedStateHandle[SHOW_NOTIFICATION_RATIONALE] = false
            eventBus.send(WelcomeFlowEvent.RequestPermissionRequest)
        }
    }

    override fun onPermissionResponse() {
        concludeWelcomeFlow()
    }

    sealed class WelcomeFlowEvent {
        object WelcomeFlowConcluded : WelcomeFlowEvent()
        data class ShowUiMessage(val uiText: UiText) : WelcomeFlowEvent()
        object RequestPermissionRequest : WelcomeFlowEvent()
        data class LaunchGoogleSignIn(val intent: Intent) : WelcomeFlowEvent()
        object RestartApplication : WelcomeFlowEvent()
    }
}

private const val FLOW_STOP = "FLOW_STOP"
private const val INCOME_INPUT = "INCOME_INPUT"
private const val SHOW_NOTIFICATION_RATIONALE = "SHOW_NOTIFICATION_RATIONALE"