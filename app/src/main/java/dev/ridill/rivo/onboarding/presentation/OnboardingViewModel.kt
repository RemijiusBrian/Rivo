package dev.ridill.rivo.onboarding.presentation

import android.Manifest
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.WorkInfo
import com.zhuinden.flowcombinetuplekt.combineTuple
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.ridill.rivo.R
import dev.ridill.rivo.core.data.preferences.PreferencesManager
import dev.ridill.rivo.core.domain.crypto.CryptoManager
import dev.ridill.rivo.core.domain.model.AuthState
import dev.ridill.rivo.core.domain.model.Resource
import dev.ridill.rivo.core.domain.model.Result
import dev.ridill.rivo.core.domain.util.BuildUtil
import dev.ridill.rivo.core.domain.util.EventBus
import dev.ridill.rivo.core.domain.util.Zero
import dev.ridill.rivo.core.domain.util.logI
import dev.ridill.rivo.core.ui.authentication.CredentialService
import dev.ridill.rivo.core.ui.util.UiText
import dev.ridill.rivo.onboarding.domain.model.OnboardingPage
import dev.ridill.rivo.settings.domain.backup.BackupWorkManager
import dev.ridill.rivo.settings.domain.modal.BackupDetails
import dev.ridill.rivo.settings.domain.repositoty.AuthRepository
import dev.ridill.rivo.settings.domain.repositoty.BackupRepository
import dev.ridill.rivo.settings.domain.repositoty.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val eventBus: EventBus<OnboardingEvent>,
    private val backupWorkManager: BackupWorkManager,
    private val settingsRepository: SettingsRepository,
    private val preferencesManager: PreferencesManager,
    private val backupRepo: BackupRepository,
    private val authRepo: AuthRepository,
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
        combineTuple(
            backupWorkManager.getRestoreDataDownloadWorkInfoFlow(),
            backupWorkManager.getImmediateDataRestoreWorkInfoFlow()
        ).collectLatest { (downloadInfo, restoreInfo) ->
            val isDownloadRunning = downloadInfo?.state == WorkInfo.State.RUNNING
            val isRestoreRunning = restoreInfo?.state == WorkInfo.State.RUNNING

            _isLoading.update { isDownloadRunning || isRestoreRunning }
            savedStateHandle[RESTORE_STATE_TEXT] = when {
                isDownloadRunning -> UiText.StringResource(R.string.downloading_app_data)
                isRestoreRunning -> UiText.StringResource(R.string.data_restore_in_progress)
                restoreInfo?.state == WorkInfo.State.SUCCEEDED -> UiText.StringResource(R.string.restarting_app)
                else -> null
            }

            when {
                restoreInfo?.state == WorkInfo.State.SUCCEEDED -> {
                    savedStateHandle[AVAILABLE_BACKUP] = null
                    preferencesManager.concludeOnboarding()
                    eventBus.send(OnboardingEvent.RestartApplication)
                }

                downloadInfo?.state == WorkInfo.State.FAILED -> {
                    eventBus.send(
                        OnboardingEvent.ShowUiMessage(
                            downloadInfo.outputData.getString(BackupWorkManager.KEY_MESSAGE)
                                ?.let { UiText.DynamicString(it) }
                                ?: UiText.StringResource(R.string.error_app_data_restore_failed)
                        )
                    )
                }

                restoreInfo?.state == WorkInfo.State.FAILED -> {
                    eventBus.send(
                        OnboardingEvent.ShowUiMessage(
                            restoreInfo.outputData.getString(BackupWorkManager.KEY_MESSAGE)
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
        viewModelScope.launch {
            val isSMSPermissionGranted = result[Manifest.permission.RECEIVE_SMS] == true
            preferencesManager.updateTransactionAutoDetectEnabled(isSMSPermissionGranted)

            val areAllGranted = result.all { it.value }
            if (areAllGranted)
                eventBus.send(OnboardingEvent.NavigateToPage(OnboardingPage.GOOGLE_SIGN_IN))
        }
    }

    fun onAccountPageReached() = viewModelScope.launch {
        val isUserUnAuthenticated = authRepo.getAuthState().first() == AuthState.UnAuthenticated
        if (isUserUnAuthenticated) {
            eventBus.send(OnboardingEvent.StartAutoSignInFlow(true))
        }
    }

    fun onCredentialResult(
        result: Result<String, CredentialService.CredentialError>
    ) = viewModelScope.launch {
        when (result) {
            is Result.Error -> {
                when (result.error) {
                    CredentialService.CredentialError.NO_AUTHORIZED_CREDENTIAL -> {
                        eventBus.send(OnboardingEvent.StartAutoSignInFlow(false))
                    }

                    CredentialService.CredentialError.CREDENTIAL_PROCESS_FAILED -> eventBus.send(
                        OnboardingEvent.ShowUiMessage(result.message)
                    )
                }
            }

            is Result.Success -> {
                signInUser(result.data)
            }
        }
    }

    private suspend fun signInUser(idToken: String) {
        when (val result = authRepo.signUserInWithToken(idToken)) {
            is Result.Error -> {
                eventBus.send(OnboardingEvent.ShowUiMessage(result.message))
            }

            is Result.Success -> {
                eventBus.send(OnboardingEvent.NavigateToPage(OnboardingPage.SET_BUDGET))
            }
        }
    }

    override fun onSkipGoogleSignInClick() {
        viewModelScope.launch {
            eventBus.send(OnboardingEvent.NavigateToPage(OnboardingPage.SET_BUDGET))
        }
    }

    private suspend fun checkForBackup() {
        savedStateHandle[RESTORE_STATE_TEXT] = UiText.StringResource(R.string.checking_for_backups)
        _isLoading.update { true }
        logI { "Running Backup Check" }
        when (val resource = backupRepo.checkForBackup()) {
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
        backupWorkManager.runImmediateRestoreWork(
            backupDetails = backupDetails,
            passwordHash = passwordHash
        )
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

    override fun onStartBudgetingClick() {
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

    sealed interface OnboardingEvent {
        data class NavigateToPage(val page: OnboardingPage) : OnboardingEvent
        data object OnboardingConcluded : OnboardingEvent
        data class ShowUiMessage(val uiText: UiText) : OnboardingEvent
        data object LaunchNotificationPermissionRequest : OnboardingEvent
        data class StartAutoSignInFlow(val filterByAuthorizedAccounts: Boolean) : OnboardingEvent
        data object RestartApplication : OnboardingEvent
    }
}

private const val BUDGET_INPUT = "BUDGET_INPUT"
private const val RESTORE_STATE_TEXT = "RESTORE_STATE_TEXT"
private const val AVAILABLE_BACKUP = "AVAILABLE_BACKUP"
private const val SHOW_ENCRYPTION_PASSWORD_INPUT = "SHOW_ENCRYPTION_PASSWORD_INPUT"