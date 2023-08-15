package dev.ridill.mym.welcomeFlow.presentation

import android.content.Intent
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.WorkInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.ridill.mym.R
import dev.ridill.mym.core.data.preferences.PreferencesManager
import dev.ridill.mym.core.domain.service.GoogleSignInService
import dev.ridill.mym.core.domain.util.EventBus
import dev.ridill.mym.core.domain.util.Zero
import dev.ridill.mym.core.ui.util.UiText
import dev.ridill.mym.settings.domain.backup.BackupWorkManager
import dev.ridill.mym.settings.domain.repositoty.SettingsRepository
import dev.ridill.mym.welcomeFlow.domain.model.WelcomeFlowStop
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WelcomeFlowViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val eventBus: EventBus<WelcomeFlowEvent>,
    private val signInService: GoogleSignInService,
    private val backupWorkManager: BackupWorkManager,
    private val settingsRepository: SettingsRepository,
    private val preferencesManager: PreferencesManager
) : ViewModel(), WelcomeFlowActions {
    val currentFlowStop = savedStateHandle.getStateFlow(FLOW_STOP, WelcomeFlowStop.WELCOME)
    val budgetInput = savedStateHandle.getStateFlow(BUDGET_INPUT, "")

    val restoreState = savedStateHandle.getStateFlow<WorkInfo.State?>(RESTORE_JOB_STATE, null)

    val events = eventBus.eventFlow

    init {
        collectRestoreWorkState()
    }

    private fun collectRestoreWorkState() = viewModelScope.launch {
        backupWorkManager.getImmediateRestoreWorkInfoFlow().collectLatest { info ->
            val state = info?.state
            savedStateHandle[RESTORE_JOB_STATE] = state

            when (state) {
                WorkInfo.State.SUCCEEDED -> {
                    preferencesManager.concludeWelcomeFlow()
                    eventBus.send(WelcomeFlowEvent.RestartApplication)
                }

                WorkInfo.State.FAILED -> {
                    eventBus.send(WelcomeFlowEvent.ShowUiMessage(UiText.StringResource(R.string.error_app_data_restore_failed)))
                }

                else -> {}
            }
        }
    }

    override fun onWelcomeMessageContinue() {
        savedStateHandle[FLOW_STOP] = WelcomeFlowStop.PERMISSIONS
    }

    override fun onPermissionsContinue() {
        viewModelScope.launch {
            eventBus.send(WelcomeFlowEvent.LaunchPermissionRequests)
        }
    }

    override fun onPermissionResponse() {
        viewModelScope.launch {
            savedStateHandle[FLOW_STOP] = WelcomeFlowStop.RESTORE_DATA
        }
    }

    override fun onCheckForBackupClick() {
        viewModelScope.launch {
            val signInIntent = signInService.getSignInIntent()
            eventBus.send(WelcomeFlowEvent.LaunchGoogleSignIn(signInIntent))
        }
    }

    fun onSignInResult(intent: Intent?) = viewModelScope.launch {
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

    private fun tryRestoreDataIfBackupExits() {
        backupWorkManager.runImmediateRestoreWork()
    }

    override fun onSkipDataRestore() {
        viewModelScope.launch {
            savedStateHandle[FLOW_STOP] = WelcomeFlowStop.SET_BUDGET
        }
    }

    override fun onBudgetInputChange(value: String) {
        savedStateHandle[BUDGET_INPUT] = value
    }

    override fun onSetBudgetContinue() {
        viewModelScope.launch {
            val budgetValue = budgetInput.value.toLongOrNull() ?: -1L
            if (budgetValue <= Long.Zero) {
                eventBus.send(
                    WelcomeFlowEvent.ShowUiMessage(
                        UiText.StringResource(R.string.error_invalid_amount, true)
                    )
                )
                return@launch
            }
            settingsRepository.updateCurrentBudget(budgetValue)
            preferencesManager.concludeWelcomeFlow()
            eventBus.send(WelcomeFlowEvent.WelcomeFlowConcluded)
        }
    }

    sealed class WelcomeFlowEvent {
        object WelcomeFlowConcluded : WelcomeFlowEvent()
        data class ShowUiMessage(val uiText: UiText) : WelcomeFlowEvent()
        object LaunchPermissionRequests : WelcomeFlowEvent()
        data class LaunchGoogleSignIn(val intent: Intent) : WelcomeFlowEvent()
        object RestartApplication : WelcomeFlowEvent()
    }
}

private const val FLOW_STOP = "FLOW_STOP"
private const val BUDGET_INPUT = "BUDGET_INPUT"
private const val RESTORE_JOB_STATE = "RESTORE_JOB_STATE"