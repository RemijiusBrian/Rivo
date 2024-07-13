package dev.ridill.rivo.settings.presentation.backupSettings

import androidx.activity.result.ActivityResult
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.WorkInfo
import com.zhuinden.flowcombinetuplekt.combineTuple
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.ridill.rivo.R
import dev.ridill.rivo.core.data.preferences.PreferencesManager
import dev.ridill.rivo.core.domain.util.EventBus
import dev.ridill.rivo.core.domain.util.asStateFlow
import dev.ridill.rivo.core.domain.util.logD
import dev.ridill.rivo.core.ui.util.UiText
import dev.ridill.rivo.settings.domain.backup.BackupWorkManager
import dev.ridill.rivo.settings.domain.modal.BackupInterval
import dev.ridill.rivo.settings.domain.repositoty.BackupSettingsRepository
import dev.ridill.rivo.settings.presentation.backupEncryption.ENCRYPTION_PASSWORD_UPDATED
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BackupSettingsViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val repo: BackupSettingsRepository,
    private val preferencesManager: PreferencesManager,
    private val eventBus: EventBus<BackupSettingsEvent>
) : ViewModel(), BackupSettingsActions {

    private val backupAccountEmail = repo.getBackupAccount()
        .map { it?.email }
        .distinctUntilChanged()
    private val isAccountAdded = backupAccountEmail.map { !it.isNullOrEmpty() }
        .distinctUntilChanged()

    private val backupInterval = MutableStateFlow(BackupInterval.MANUAL)

    private val showBackupIntervalSelection = savedStateHandle
        .getStateFlow(SHOW_BACKUP_INTERVAL_SELECTION, false)

    private val lastBackupDateTime = repo.getLastBackupTime()
        .distinctUntilChanged()

    private val isBackupRunning = MutableStateFlow(false)

    private val isEncryptionPasswordAvailable = repo.isEncryptionPasswordAvailable()

    private val fatalBackupError = repo.getFatalBackupError()

    private val showBackupRunningMessage = savedStateHandle
        .getStateFlow(SHOW_BACKUP_RUNNING_MESSAGE, false)

    val state = combineTuple(
        backupAccountEmail,
        isAccountAdded,
        backupInterval,
        showBackupIntervalSelection,
        lastBackupDateTime,
        isBackupRunning,
        isEncryptionPasswordAvailable,
        fatalBackupError,
        showBackupRunningMessage
    ).map { (
                backupAccountEmail,
                isAccountAdded,
                backupInterval,
                showBackupIntervalSelection,
                lastBackupDateTime,
                isBackupRunning,
                isEncryptionPasswordAvailable,
                fatalBackupError,
                showBackupRunningMessage

            ) ->
        BackupSettingsState(
            backupAccountEmail = backupAccountEmail,
            isAccountAdded = isAccountAdded,
            backupInterval = backupInterval,
            showBackupIntervalSelection = showBackupIntervalSelection,
            lastBackupDateTime = lastBackupDateTime,
            isBackupRunning = isBackupRunning,
            isEncryptionPasswordAvailable = isEncryptionPasswordAvailable,
            fatalBackupError = fatalBackupError,
            showBackupRunningMessage = showBackupRunningMessage
        )
    }.asStateFlow(viewModelScope, BackupSettingsState())

    val events = eventBus.eventFlow

    init {
        getSignedInUser()
        collectImmediateBackupWorkInfo()
        collectPeriodicBackupWorkInfo()
    }

    private fun getSignedInUser() {
        repo.refreshBackupAccount()
    }

    private var hasBackupJobRunThisSession: Boolean = false
    private fun collectImmediateBackupWorkInfo() = viewModelScope.launch {
        repo.getImmediateBackupWorkInfo().collectLatest { info ->
            logD { "Immediate Backup Work Info - $info" }
            val isRunning = info?.state == WorkInfo.State.RUNNING
            isBackupRunning.update { isRunning }
            if (isRunning) {
                hasBackupJobRunThisSession = true
            }

            // if check to prevent showing message without running backup job at least once.
            // hasBackupJobRunThisSession boolean is set to true when backup job is running.
            // Without this check WorkInfo output message will be shown everytime user arrives at screen
            // Even if backup was run long back
            if (hasBackupJobRunThisSession) {
                info?.outputData?.getString(BackupWorkManager.KEY_MESSAGE)?.let {
                    eventBus.send(BackupSettingsEvent.ShowUiMessage(UiText.DynamicString(it)))
                }
            }
        }
    }

    private fun collectPeriodicBackupWorkInfo() = viewModelScope.launch {
        repo.getPeriodicBackupWorkInfo().collectLatest { info ->
            updateBackupInterval(info)
            logD { "Periodic Backup Work Info - $info" }
            isBackupRunning.update {
                info?.state == WorkInfo.State.RUNNING
            }
        }
    }

    private fun updateBackupInterval(info: WorkInfo?) = viewModelScope.launch {
        val interval = if (info?.state == WorkInfo.State.CANCELLED)
            BackupInterval.MANUAL
        else info?.let(repo::getIntervalFromInfo)
        backupInterval.update { interval ?: BackupInterval.MANUAL }
    }

    override fun onBackupAccountClick() {
        if (isBackupRunning.value) {
            savedStateHandle[SHOW_BACKUP_RUNNING_MESSAGE] = true
            return
        }
        viewModelScope.launch {
            eventBus.send(BackupSettingsEvent.LaunchGoogleSignIn)
        }
    }

    fun onSignInResult(result: ActivityResult) = viewModelScope.launch {
        /*when (val resource = repo.signInUser(result)) {
            is Resource.Error -> {
                resource.message?.let { eventBus.send(BackupSettingsEvent.ShowUiMessage(it)) }
            }

            is Resource.Success -> {
                if (preferencesManager.preferences.first().encryptionPasswordHash.isNullOrEmpty()) {
                    eventBus.send(BackupSettingsEvent.NavigateToBackupEncryptionScreen)
                }
            }
        }*/
    }

    override fun onBackupIntervalPreferenceClick() {
        viewModelScope.launch {
            if (preferencesManager.preferences.first().encryptionPasswordHash.isNullOrEmpty()) {
                eventBus.send(BackupSettingsEvent.NavigateToBackupEncryptionScreen)
                return@launch
            }
            savedStateHandle[SHOW_BACKUP_INTERVAL_SELECTION] = true
        }
    }

    override fun onBackupIntervalSelected(interval: BackupInterval) {
        viewModelScope.launch {
            savedStateHandle[SHOW_BACKUP_INTERVAL_SELECTION] = false
            repo.updateBackupIntervalAndScheduleJob(interval)
        }
    }

    override fun onBackupIntervalSelectionDismiss() {
        savedStateHandle[SHOW_BACKUP_INTERVAL_SELECTION] = false
    }

    override fun onBackupNowClick() {
        viewModelScope.launch {
            if (preferencesManager.preferences.first().encryptionPasswordHash.isNullOrEmpty()) {
                eventBus.send(BackupSettingsEvent.NavigateToBackupEncryptionScreen)
                return@launch
            }

            repo.runImmediateBackupJob()
        }
    }

    override fun onEncryptionPreferenceClick() {
        viewModelScope.launch {
            eventBus.send(BackupSettingsEvent.NavigateToBackupEncryptionScreen)
        }
    }

    fun onDestinationResult(result: String) {
        viewModelScope.launch {
            when (result) {
                ENCRYPTION_PASSWORD_UPDATED -> {
                    eventBus.send(BackupSettingsEvent.ShowUiMessage(UiText.StringResource(R.string.encryption_password_updated)))
                    val interval = backupInterval.first()
                    repo.runBackupJob(interval)
                }

                else -> Unit
            }
        }
    }

    override fun onBackupRunningMessageAcknowledge() {
        savedStateHandle[SHOW_BACKUP_RUNNING_MESSAGE] = false
    }

    sealed interface BackupSettingsEvent {
        data class ShowUiMessage(val uiText: UiText) : BackupSettingsEvent
        data object NavigateToBackupEncryptionScreen : BackupSettingsEvent
        data object LaunchGoogleSignIn : BackupSettingsEvent
    }
}

private const val SHOW_BACKUP_INTERVAL_SELECTION = "SHOW_BACKUP_INTERVAL_SELECTION"
private const val SHOW_BACKUP_RUNNING_MESSAGE = "SHOW_BACKUP_RUNNING_MESSAGE"