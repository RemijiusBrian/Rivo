package dev.ridill.rivo.settings.presentation.backupSettings

import android.content.Intent
import androidx.activity.result.ActivityResult
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.WorkInfo
import com.zhuinden.flowcombinetuplekt.combineTuple
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.ridill.rivo.core.domain.model.Resource
import dev.ridill.rivo.core.domain.util.EventBus
import dev.ridill.rivo.core.domain.util.asStateFlow
import dev.ridill.rivo.core.ui.util.UiText
import dev.ridill.rivo.settings.domain.backup.BackupWorkManager
import dev.ridill.rivo.settings.domain.modal.BackupInterval
import dev.ridill.rivo.settings.domain.repositoty.BackupSettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BackupSettingsViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val repo: BackupSettingsRepository,
    private val eventBus: EventBus<BackupEvent>
) : ViewModel(), BackupSettingsActions {

    private val backupAccountEmail = repo.getBackupAccount()
        .map { it?.email }
        .distinctUntilChanged()
    private val isAccountAdded = backupAccountEmail.map { !it.isNullOrEmpty() }
        .distinctUntilChanged()

    private val backupInterval = MutableStateFlow(BackupInterval.MANUAL)
    private val backupUsingCellular = repo.getBackupUsingCellular()
        .distinctUntilChanged()

    private val showBackupIntervalSelection = savedStateHandle
        .getStateFlow(SHOW_BACKUP_INTERVAL_SELECTION, false)

    private val lastBackupDateTime = repo.getLastBackupTime()
        .distinctUntilChanged()

    private val isBackupRunning = MutableStateFlow(false)

    val state = combineTuple(
        backupAccountEmail,
        isAccountAdded,
        backupInterval,
        backupUsingCellular,
        showBackupIntervalSelection,
        lastBackupDateTime,
        isBackupRunning
    ).map { (
                backupAccount,
                isAccountAdded,
                backupInterval,
                backupUsingCellular,
                showBackupIntervalSelection,
                lastBackupDateTime,
                isBackupWorkerRunning
            ) ->
        BackupSettingsState(
            accountEmail = backupAccount,
            isAccountAdded = isAccountAdded,
            showBackupIntervalSelection = showBackupIntervalSelection,
            interval = backupInterval,
            backupUsingCellular = backupUsingCellular,
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

            is Resource.Success -> Unit
        }
    }

    override fun onBackupIntervalPreferenceClick() {
        savedStateHandle[SHOW_BACKUP_INTERVAL_SELECTION] = true
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
        repo.runImmediateBackupJob()
    }

    override fun onBackupUsingCellularToggle(checked: Boolean) {
        viewModelScope.launch {
            repo.updateBackupUsingCellular(checked = checked, interval = backupInterval.value)
        }
    }

    sealed class BackupEvent {
        data class ShowUiMessage(val uiText: UiText) : BackupEvent()
        data class LaunchGoogleSignIn(val intent: Intent) : BackupEvent()
    }
}

private const val SHOW_BACKUP_INTERVAL_SELECTION = "SHOW_BACKUP_INTERVAL_SELECTION"