package dev.ridill.rivo.welcomeFlow.presentation

import android.content.Intent
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.WorkInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.ridill.rivo.R
import dev.ridill.rivo.core.data.preferences.PreferencesManager
import dev.ridill.rivo.core.domain.model.Resource
import dev.ridill.rivo.core.domain.service.GoogleSignInService
import dev.ridill.rivo.core.domain.util.BuildUtil
import dev.ridill.rivo.core.domain.util.EventBus
import dev.ridill.rivo.core.domain.util.Zero
import dev.ridill.rivo.core.ui.util.UiText
import dev.ridill.rivo.settings.domain.backup.BackupWorkManager
import dev.ridill.rivo.settings.domain.modal.BackupDetails
import dev.ridill.rivo.settings.domain.repositoty.BackupRepository
import dev.ridill.rivo.settings.domain.repositoty.SettingsRepository
import dev.ridill.rivo.welcomeFlow.domain.model.WelcomeFlowPage
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
    private val preferencesManager: PreferencesManager,
    private val backupRepository: BackupRepository
) : ViewModel(), WelcomeFlowActions {
    val budgetInput = savedStateHandle.getStateFlow(BUDGET_INPUT, "")

    val restoreState = savedStateHandle.getStateFlow<WorkInfo.State?>(RESTORE_JOB_STATE, null)

    val availableBackup = savedStateHandle.getStateFlow<BackupDetails?>(AVAILABLE_BACKUP, null)

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
                    savedStateHandle[AVAILABLE_BACKUP] = null
                    preferencesManager.concludeWelcomeFlow()
                    eventBus.send(WelcomeFlowEvent.RestartApplication)
                }

                WorkInfo.State.FAILED -> {
                    eventBus.send(
                        WelcomeFlowEvent.ShowUiMessage(
                            info.outputData.getString(BackupWorkManager.KEY_MESSAGE)
                                ?.let { UiText.DynamicString(it) }
                                ?: UiText.StringResource(R.string.error_app_data_restore_failed)
                        )
                    )
                }

                else -> Unit
            }
        }
    }

    override fun onGiveNotificationPermissionClick() {
        viewModelScope.launch {
            if (BuildUtil.isNotificationRuntimePermissionNeeded())
                eventBus.send(WelcomeFlowEvent.LaunchNotificationPermissionRequest)
            else
                eventBus.send(WelcomeFlowEvent.NavigateToPage(WelcomeFlowPage.GOOGLE_SIGN_IN))
        }
    }

    override fun onSkipNotificationPermission() {
        viewModelScope.launch {
            eventBus.send(WelcomeFlowEvent.NavigateToPage(WelcomeFlowPage.GOOGLE_SIGN_IN))
        }
    }

    override fun onNotificationPermissionResponse(granted: Boolean) {
        if (granted) viewModelScope.launch {
            eventBus.send(WelcomeFlowEvent.NavigateToPage(WelcomeFlowPage.GOOGLE_SIGN_IN))
        }
    }

    override fun onGoogleSignInClick() {
        viewModelScope.launch {
            val signInIntent = signInService.getSignInIntent()
            eventBus.send(WelcomeFlowEvent.LaunchGoogleSignIn(signInIntent))
        }
    }

    override fun onSkipGoogleSignInClick() {
        viewModelScope.launch {
            eventBus.send(WelcomeFlowEvent.NavigateToPage(WelcomeFlowPage.SET_BUDGET))
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
        checkForBackup()
    }

    private suspend fun checkForBackup() {
        when (val resource = backupRepository.checkForBackup()) {
            is Resource.Error -> {
                eventBus.send(WelcomeFlowEvent.NavigateToPage(WelcomeFlowPage.SET_BUDGET))
            }

            is Resource.Success -> {
                if (resource.data != null) {
                    savedStateHandle[AVAILABLE_BACKUP] = resource.data
                } else {
                    eventBus.send(WelcomeFlowEvent.NavigateToPage(WelcomeFlowPage.SET_BUDGET))
                }
            }
        }
    }

    override fun onRestoreDataClick() {
        viewModelScope.launch {
            val backupDetails = availableBackup.value
            if (backupDetails == null) {
                eventBus.send(WelcomeFlowEvent.NavigateToPage(WelcomeFlowPage.SET_BUDGET))
                return@launch
            }
            backupWorkManager.runImmediateRestoreWork(backupDetails)
        }
    }

    override fun onSkipDataRestore() {
        viewModelScope.launch {
            eventBus.send(WelcomeFlowEvent.NavigateToPage(WelcomeFlowPage.SET_BUDGET))
            savedStateHandle[AVAILABLE_BACKUP] = null
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
        data class NavigateToPage(val page: WelcomeFlowPage) : WelcomeFlowEvent()
        object WelcomeFlowConcluded : WelcomeFlowEvent()
        data class ShowUiMessage(val uiText: UiText) : WelcomeFlowEvent()
        object LaunchNotificationPermissionRequest : WelcomeFlowEvent()
        data class LaunchGoogleSignIn(val intent: Intent) : WelcomeFlowEvent()
        object RestartApplication : WelcomeFlowEvent()
    }
}

private const val BUDGET_INPUT = "BUDGET_INPUT"
private const val RESTORE_JOB_STATE = "RESTORE_JOB_STATE"
private const val AVAILABLE_BACKUP = "AVAILABLE_BACKUP"