package dev.ridill.rivo.settings.presentation.backup

import android.content.Intent
import androidx.activity.result.ActivityResult
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.WorkInfo
import com.zhuinden.flowcombinetuplekt.combineTuple
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.ridill.rivo.core.data.preferences.PreferencesManager
import dev.ridill.rivo.core.domain.model.Resource
import dev.ridill.rivo.core.domain.util.EventBus
import dev.ridill.rivo.core.domain.util.asStateFlow
import dev.ridill.rivo.core.domain.util.logD
import dev.ridill.rivo.core.ui.util.UiText
import dev.ridill.rivo.settings.domain.backup.BackupWorkManager
import dev.ridill.rivo.settings.domain.modal.BackupInterval
import dev.ridill.rivo.settings.domain.repositoty.BackupSettingsRepository
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
    private val eventBus: EventBus<BackupEvent>
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

    val state = combineTuple(
        backupAccountEmail,
        isAccountAdded,
        backupInterval,
        showBackupIntervalSelection,
        lastBackupDateTime,
        isBackupRunning
    ).map { (
                backupAccount,
                isAccountAdded,
                backupInterval,
                showBackupIntervalSelection,
                lastBackupDateTime,
                isBackupWorkerRunning
            ) ->
        BackupSettingsState(
            accountEmail = backupAccount,
            isAccountAdded = isAccountAdded,
            showBackupIntervalSelection = showBackupIntervalSelection,
            interval = backupInterval,
            lastBackupDateTime = lastBackupDateTime,
            isBackupRunning = isBackupWorkerRunning
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
                    eventBus.send(BackupEvent.ShowUiMessage(UiText.DynamicString(it)))
                }
            }
        }
    }

    private fun collectPeriodicBackupWorkInfo() = viewModelScope.launch {
        repo.getPeriodicBackupWorkInfo().collectLatest { info ->
            isBackupRunning.update {
                info?.state == WorkInfo.State.RUNNING
            }
            setBackupIntervalFromWorkInfo(info)
            logD { "Backup Work Info - $info" }
        }
    }

    private fun setBackupIntervalFromWorkInfo(info: WorkInfo?) {
        val intervalTagIndex = info?.tags
            ?.indexOfFirst { it.startsWith(BackupWorkManager.WORK_INTERVAL_TAG_PREFIX) }
            ?: -1

        val intervalTag = info?.tags?.elementAtOrNull(intervalTagIndex)
            ?.removePrefix(BackupWorkManager.WORK_INTERVAL_TAG_PREFIX)
            ?.takeIf { info.state != WorkInfo.State.CANCELLED }

        val interval = BackupInterval.valueOf(
            intervalTag ?: BackupInterval.MANUAL.name
        )
        backupInterval.update { interval }
    }

    override fun onBackupAccountClick() {
        viewModelScope.launch {
            val intent = repo.getSignInIntent()
            eventBus.send(BackupEvent.LaunchGoogleSignIn(intent))
        }
    }

    fun onSignInResult(result: ActivityResult) = viewModelScope.launch {
        when (val resource = repo.signInUser(result)) {
            is Resource.Error -> {
                resource.message?.let { eventBus.send(BackupEvent.ShowUiMessage(it)) }
            }

            is Resource.Success -> {
                eventBus.send(BackupEvent.NavigateToBackupEncryptionScreen)
            }
        }
    }

    override fun onBackupIntervalPreferenceClick() {
        viewModelScope.launch {
            if (preferencesManager.preferences.first().encryptionPasswordHash.isNullOrEmpty()) {
                eventBus.send(BackupEvent.NavigateToBackupEncryptionScreen)
                return@launch
            }
            savedStateHandle[SHOW_BACKUP_INTERVAL_SELECTION] = true
        }
    }

    override fun onBackupIntervalSelected(interval: BackupInterval) {
        viewModelScope.launch {
            savedStateHandle[SHOW_BACKUP_INTERVAL_SELECTION] = false
            repo.updateBackupInterval(interval)
        }
    }

    override fun onBackupIntervalSelectionDismiss() {
        savedStateHandle[SHOW_BACKUP_INTERVAL_SELECTION] = false
    }

    override fun onBackupNowClick() {
        viewModelScope.launch {
            if (preferencesManager.preferences.first().encryptionPasswordHash.isNullOrEmpty()) {
                eventBus.send(BackupEvent.NavigateToBackupEncryptionScreen)
                return@launch
            }
            repo.runImmediateBackupJob()
        }
    }

    override fun onEncryptionPreferenceClick() {
        viewModelScope.launch {
            eventBus.send(BackupEvent.NavigateToBackupEncryptionScreen)
        }
    }

    sealed class BackupEvent {
        data class ShowUiMessage(val uiText: UiText) : BackupEvent()
        data object NavigateToBackupEncryptionScreen : BackupEvent()
        data class LaunchGoogleSignIn(val intent: Intent) : BackupEvent()
    }
}

private const val SHOW_BACKUP_INTERVAL_SELECTION = "SHOW_BACKUP_INTERVAL_SELECTION"