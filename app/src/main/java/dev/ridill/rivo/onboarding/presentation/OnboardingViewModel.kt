package dev.ridill.rivo.onboarding.presentation

import android.content.Intent
import androidx.activity.result.ActivityResult
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.WorkInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.ridill.rivo.R
import dev.ridill.rivo.core.data.preferences.PreferencesManager
import dev.ridill.rivo.core.domain.crypto.CryptoManager
import dev.ridill.rivo.core.domain.model.Resource
import dev.ridill.rivo.core.domain.service.GoogleSignInService
import dev.ridill.rivo.core.domain.util.BuildUtil
import dev.ridill.rivo.core.domain.util.EventBus
import dev.ridill.rivo.core.domain.util.Zero
import dev.ridill.rivo.core.domain.util.logI
import dev.ridill.rivo.core.ui.util.UiText
import dev.ridill.rivo.onboarding.domain.model.OnboardingPage
import dev.ridill.rivo.settings.domain.backup.BackupWorkManager
import dev.ridill.rivo.settings.domain.modal.BackupDetails
import dev.ridill.rivo.settings.domain.repositoty.BackupRepository
import dev.ridill.rivo.settings.domain.repositoty.SettingsRepository
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val eventBus: EventBus<OnboardingEvent>,
    private val signInService: GoogleSignInService,
    private val backupWorkManager: BackupWorkManager,
    private val settingsRepository: SettingsRepository,
    private val preferencesManager: PreferencesManager,
    private val backupRepository: BackupRepository,
    private val cryptoManager: CryptoManager
) : ViewModel(), OnboardingActions {
    private val restoreState = savedStateHandle
        .getStateFlow<WorkInfo.State?>(RESTORE_JOB_STATE, null)
    val restoreStatusText = restoreState.map { state ->
        when (state) {
            WorkInfo.State.RUNNING -> UiText.StringResource(R.string.data_restore_in_progress)
            WorkInfo.State.SUCCEEDED -> UiText.StringResource(R.string.restarting_app)
            else -> null
        }
    }.distinctUntilChanged()
    val isRestoreRunning = restoreState.map { it == WorkInfo.State.RUNNING }
        .distinctUntilChanged()

    val availableBackup = savedStateHandle.getStateFlow<BackupDetails?>(AVAILABLE_BACKUP, null)

    val currency = settingsRepository.getCurrencyPreference()
    val budgetInput = savedStateHandle.getStateFlow(BUDGET_INPUT, "")
    val showEncryptionPasswordInput = savedStateHandle
        .getStateFlow(SHOW_ENCRYPTION_PASSWORD_INPUT, false)

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
                    preferencesManager.concludeOnboarding()
                    eventBus.send(OnboardingEvent.RestartApplication)
                }

                WorkInfo.State.FAILED -> {
                    eventBus.send(
                        OnboardingEvent.ShowUiMessage(
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
                eventBus.send(OnboardingEvent.LaunchNotificationPermissionRequest)
            else
                eventBus.send(OnboardingEvent.NavigateToPage(OnboardingPage.GOOGLE_SIGN_IN))
        }
    }

    override fun onSkipNotificationPermission() {
        viewModelScope.launch {
            eventBus.send(OnboardingEvent.NavigateToPage(OnboardingPage.GOOGLE_SIGN_IN))
        }
    }

    override fun onNotificationPermissionResponse(granted: Boolean) {
        if (granted) viewModelScope.launch {
            eventBus.send(OnboardingEvent.NavigateToPage(OnboardingPage.GOOGLE_SIGN_IN))
        }
    }

    override fun onGoogleSignInClick() {
        viewModelScope.launch {
            val signInIntent = signInService.getSignInIntent()
            eventBus.send(OnboardingEvent.LaunchGoogleSignIn(signInIntent))
        }
    }

    override fun onSkipGoogleSignInClick() {
        viewModelScope.launch {
            eventBus.send(OnboardingEvent.NavigateToPage(OnboardingPage.SET_BUDGET))
        }
    }

    fun onSignInResult(result: ActivityResult) = viewModelScope.launch {
        when (val resource = signInService.getAccountFromSignInResult(result)) {
            is Resource.Error -> {
                resource.message?.let { eventBus.send(OnboardingEvent.ShowUiMessage(it)) }
            }

            is Resource.Success -> {
                checkForBackup()
            }
        }
    }

    private suspend fun checkForBackup() {
        logI { "Running Backup Check" }
        when (val resource = backupRepository.checkForBackup()) {
            is Resource.Error -> {
                eventBus.send(OnboardingEvent.NavigateToPage(OnboardingPage.SET_BUDGET))
            }

            is Resource.Success -> {
                if (resource.data != null) {
                    savedStateHandle[AVAILABLE_BACKUP] = resource.data
                } else {
                    eventBus.send(OnboardingEvent.NavigateToPage(OnboardingPage.SET_BUDGET))
                }
            }
        }
    }

    override fun onRestoreDataClick() {
        viewModelScope.launch {
            val backupDetails = availableBackup.value
            if (backupDetails == null) {
                eventBus.send(OnboardingEvent.NavigateToPage(OnboardingPage.SET_BUDGET))
                return@launch
            }
            savedStateHandle[SHOW_ENCRYPTION_PASSWORD_INPUT] = true
        }
    }

    override fun onEncryptionPasswordInputDismiss() {
        savedStateHandle[SHOW_ENCRYPTION_PASSWORD_INPUT] = false
    }

    override fun onEncryptionPasswordSubmit(password: String) {
        val backupDetails = availableBackup.value ?: return
        val passwordHash = cryptoManager.hash(password)
        savedStateHandle[SHOW_ENCRYPTION_PASSWORD_INPUT] = false
        backupWorkManager.runImmediateRestoreWork(backupDetails, passwordHash)
    }

    override fun onSkipDataRestore() {
        viewModelScope.launch {
            eventBus.send(OnboardingEvent.NavigateToPage(OnboardingPage.SET_BUDGET))
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
                    OnboardingEvent.ShowUiMessage(
                        UiText.StringResource(R.string.error_invalid_amount, true)
                    )
                )
                return@launch
            }
            settingsRepository.updateCurrentBudget(budgetValue)
            preferencesManager.concludeOnboarding()
            eventBus.send(OnboardingEvent.OnboardingConcluded)
        }
    }

    sealed class OnboardingEvent {
        data class NavigateToPage(val page: OnboardingPage) : OnboardingEvent()
        data object OnboardingConcluded : OnboardingEvent()
        data class ShowUiMessage(val uiText: UiText) : OnboardingEvent()
        data object LaunchNotificationPermissionRequest : OnboardingEvent()
        data class LaunchGoogleSignIn(val intent: Intent) : OnboardingEvent()
        data object RestartApplication : OnboardingEvent()
    }
}

private const val BUDGET_INPUT = "BUDGET_INPUT"
private const val RESTORE_JOB_STATE = "RESTORE_JOB_STATE"
private const val AVAILABLE_BACKUP = "AVAILABLE_BACKUP"
private const val SHOW_ENCRYPTION_PASSWORD_INPUT = "SHOW_ENCRYPTION_PASSWORD_INPUT"