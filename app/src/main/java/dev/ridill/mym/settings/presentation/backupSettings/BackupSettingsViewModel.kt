package dev.ridill.mym.settings.presentation.backupSettings

import android.app.Activity
import android.content.Intent
import androidx.activity.result.ActivityResult
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.WorkInfo
import com.zhuinden.flowcombinetuplekt.combineTuple
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.ridill.mym.R
import dev.ridill.mym.core.data.preferences.PreferencesManager
import dev.ridill.mym.core.domain.service.GoogleSignInService
import dev.ridill.mym.core.domain.util.EventBus
import dev.ridill.mym.core.domain.util.asStateFlow
import dev.ridill.mym.core.domain.util.tryOrNull
import dev.ridill.mym.core.ui.util.UiText
import dev.ridill.mym.settings.domain.backup.BackupWorkManager
import dev.ridill.mym.settings.domain.modal.BackupInterval
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BackupSettingsViewModel @Inject constructor(
    private val eventBus: EventBus<BackupEvent>,
    private val signInService: GoogleSignInService,
    preferencesManager: PreferencesManager,
    private val savedStateHandle: SavedStateHandle,
    private val backupWorkManager: BackupWorkManager
) : ViewModel(), BackupSettingsActions {

    private val backupAccount = MutableStateFlow<String?>(null)
    private val isAccountAdded = backupAccount.map { !it.isNullOrEmpty() }
        .distinctUntilChanged()

    private val preferences = preferencesManager.preferences

    private val backupInterval = MutableStateFlow(BackupInterval.MANUAL)

    private val showBackupIntervalSelection = savedStateHandle
        .getStateFlow(SHOW_BACKUP_INTERVAL_SELECTION, false)

    private val lastBackupDateTime = preferences.map { it.lastBackupDateTime }
        .distinctUntilChanged()

    private val isBackupWorkerRunning = MutableStateFlow(false)

    val state = combineTuple(
        backupAccount,
        isAccountAdded,
        backupInterval,
        showBackupIntervalSelection,
        lastBackupDateTime,
        isBackupWorkerRunning
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
        collectImmediateBackupWorkState()
        collectPeriodicBackupWorkInfo()
    }

    private fun getSignedInUser() {
        backupAccount.update {
            signInService.getSignedInAccount()?.email
        }
    }

    private fun collectImmediateBackupWorkState() = viewModelScope.launch {
        backupWorkManager.getImmediateBackupWorkInfoFlow()
            .collect { info ->
                isBackupWorkerRunning.update {
                    info?.state == WorkInfo.State.RUNNING
                }
            }
    }

    private fun collectPeriodicBackupWorkInfo() = viewModelScope.launch {
        backupWorkManager.getPeriodicBackupWorkInfoFlow()
            .collectLatest { info ->
                updateBackupInterval(info)
                isBackupWorkerRunning.update { info?.state == WorkInfo.State.RUNNING }

                when (info?.state) {
                    WorkInfo.State.SUCCEEDED -> {
                        eventBus.send(BackupEvent.ShowUiMessage(UiText.StringResource(R.string.backup_complete)))
                    }

                    WorkInfo.State.FAILED -> {
                        eventBus.send(BackupEvent.ShowUiMessage(UiText.StringResource(R.string.error_app_data_backup_failed)))
                    }

                    else -> Unit
                }
            }
    }

    private fun updateBackupInterval(info: WorkInfo?) {
        if (info?.state == WorkInfo.State.CANCELLED) {
            backupInterval.update { BackupInterval.MANUAL }
        } else {
            val indexOfIntervalTag = info?.tags
                ?.indexOfFirst { it.startsWith(BackupWorkManager.INTERVAL_TAG_PREFIX) }
                ?: -1
            val workInterval = BackupInterval.valueOf(
                info?.tags?.elementAtOrNull(indexOfIntervalTag)
                    ?.removePrefix(BackupWorkManager.INTERVAL_TAG_PREFIX)
                    ?: BackupInterval.MANUAL.name
            )
            backupInterval.update { workInterval }
        }
    }

    override fun onBackupAccountClick() {
        viewModelScope.launch {
            val intent = signInService.getSignInIntent()
            eventBus.send(BackupEvent.LaunchGoogleSignIn(intent))
        }
    }

    fun onSignInResult(result: ActivityResult) = viewModelScope.launch {
        if (result.resultCode != Activity.RESULT_OK) {
            backupAccount.update { null }
            return@launch
        }
        val account = tryOrNull { signInService.getAccountFromIntent(result.data) }
        backupAccount.update { account?.email }

        if (account == null) {
            eventBus.send(
                BackupEvent.ShowUiMessage(
                    UiText.StringResource(
                        R.string.error_sign_in_failed,
                        true
                    )
                )
            )
            return@launch
        }
    }

    override fun onBackupIntervalPreferenceClick() {
        savedStateHandle[SHOW_BACKUP_INTERVAL_SELECTION] = true
    }

    override fun onBackupIntervalSelected(interval: BackupInterval) {
        viewModelScope.launch {
            savedStateHandle[SHOW_BACKUP_INTERVAL_SELECTION] = false
            if (signInService.getSignedInAccount() == null) return@launch

            if (interval == BackupInterval.MANUAL)
                backupWorkManager.cancelPeriodicBackupWork()
            else
                backupWorkManager.schedulePeriodicWorker(interval)
        }
    }

    override fun onBackupIntervalSelectionDismiss() {
        savedStateHandle[SHOW_BACKUP_INTERVAL_SELECTION] = false
    }

    override fun onBackupNowClick() {
        backupWorkManager.runImmediateBackupWork()
    }

    sealed class BackupEvent {
        data class ShowUiMessage(val uiText: UiText) : BackupEvent()
        data class LaunchGoogleSignIn(val intent: Intent) : BackupEvent()
    }
}

private const val SHOW_BACKUP_INTERVAL_SELECTION = "SHOW_BACKUP_INTERVAL_SELECTION"