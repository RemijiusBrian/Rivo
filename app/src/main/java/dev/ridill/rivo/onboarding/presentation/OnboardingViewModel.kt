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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
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
    val restoreStatusText = savedStateHandle
        .getStateFlow<UiText?>(RESTORE_STATE_TEXT, null)

    private val _isLoading = MutableStateFlow(false)
    val isLoading get() = _isLoading.asStateFlow()

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
            _isLoading.update { state == WorkInfo.State.RUNNING }
            savedStateHandle[RESTORE_STATE_TEXT] = when (state) {
                WorkInfo.State.RUNNING -> UiText.StringResource(R.string.data_restore_in_progress)
                WorkInfo.State.SUCCEEDED -> UiText.StringResource(R.string.restarting_app)
                else -> null
            }

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

    override fun onGivePermissionsClick() {
        viewModelScope.launch {
            if (BuildUtil.isNotificationRuntimePermissionNeeded())
                eventBus.send(OnboardingEvent.LaunchNotificationPermissionRequest)
            else
                eventBus.send(OnboardingEvent.NavigateToPage(OnboardingPage.GOOGLE_SIGN_IN))
        }
    }

    override fun onSkipPermissionsClick() {
        viewModelScope.launch {
            eventBus.send(OnboardingEvent.NavigateToPage(OnboardingPage.GOOGLE_SIGN_IN))
        }
    }

    fun onPermissionsRequestResult(result: Map<String, Boolean>) {
        val areAllGranted = result.all { it.value }
        if (areAllGranted) viewModelScope.launch {
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
        savedStateHandle[RESTORE_STATE_TEXT] = UiText.StringResource(R.string.checking_for_backups)
        _isLoading.update { true }
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
        _isLoading.update { false }
        savedStateHandle[RESTORE_STATE_TEXT] = null
    }

    override fun onRestoreDataClick() {
        viewModelScope.launch {
            if (isLoading.value) return@launch
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
private const val RESTORE_STATE_TEXT = "RESTORE_STATE_TEXT"
private const val AVAILABLE_BACKUP = "AVAILABLE_BACKUP"
private const val SHOW_ENCRYPTION_PASSWORD_INPUT = "SHOW_ENCRYPTION_PASSWORD_INPUT"