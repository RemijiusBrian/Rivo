package dev.ridill.mym.welcomeFlow.presentation

import android.content.Intent
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import androidx.work.WorkInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.ridill.mym.R
import dev.ridill.mym.core.data.preferences.PreferencesManager
import dev.ridill.mym.core.domain.service.GoogleSignInService
import dev.ridill.mym.core.domain.util.BuildUtil
import dev.ridill.mym.core.domain.util.EventBus
import dev.ridill.mym.core.domain.util.Zero
import dev.ridill.mym.core.ui.util.UiText
import dev.ridill.mym.settings.domain.backup.BackupWorkManager
import dev.ridill.mym.welcomeFlow.domain.model.WelcomeFlowStop
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WelcomeFlowViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val preferenceManager: PreferencesManager,
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

    val restoreState = savedStateHandle.getStateFlow(RESTORE_JOB_STATE, WorkInfo.State.BLOCKED)

    val events = eventBus.eventFlow

    override fun onNextClick() {
        when (currentFlowStop.value) {
            WelcomeFlowStop.WELCOME -> {
                savedStateHandle[FLOW_STOP] = WelcomeFlowStop.RESTORE_DATA
            }

            WelcomeFlowStop.RESTORE_DATA -> {}

            WelcomeFlowStop.INCOME_SET -> {
                updateLimitAndContinue()
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

        tryRestoreDataIfBackupExits()
    }

    private var restoreJob: Job? = null
    private fun tryRestoreDataIfBackupExits() {
        restoreJob?.cancel()
        restoreJob = viewModelScope.launch {
            backupWorkManager.runRestoreWorkerNow().asFlow().collectLatest { info ->
                val state = info.state
                savedStateHandle[RESTORE_JOB_STATE] = state

                when (state) {
                    WorkInfo.State.SUCCEEDED -> {
                        preferenceManager.concludeWelcomeFlow()
                        eventBus.send(WelcomeFlowEvent.RestartApplication)
                    }

                    WorkInfo.State.FAILED -> {
                        eventBus.send(WelcomeFlowEvent.ShowUiMessage(UiText.StringResource(R.string.error_app_data_restore_failed)))
                    }

                    else -> {}
                }
            }
        }
    }

    private fun updateLimitAndContinue() = viewModelScope.launch {
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
private const val RESTORE_JOB_STATE = "RESTORE_JOB_STATE"