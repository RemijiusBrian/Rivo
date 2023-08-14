package dev.ridill.mym.settings.presentation.backupSettings

import android.app.Activity
import android.content.Intent
import androidx.activity.result.ActivityResult
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BackupSettingsViewModel @Inject constructor(
    private val eventBus: EventBus<BackupEvent>,
    private val signInService: GoogleSignInService,
    private val preferencesManager: PreferencesManager,
    private val savedStateHandle: SavedStateHandle,
    private val backupWorkManager: BackupWorkManager
) : ViewModel(), BackupSettingsActions {

    private val backupAccount = MutableStateFlow<String?>(null)
    private val isAccountAdded = backupAccount.map { !it.isNullOrEmpty() }
        .distinctUntilChanged()

    private val backupInterval = preferencesManager.preferences.map { it.appBackupInterval }
        .distinctUntilChanged()

    private val showBackupIntervalSelection = savedStateHandle
        .getStateFlow(SHOW_BACKUP_INTERVAL_SELECTION, false)

    val state = combineTuple(
        backupAccount,
        isAccountAdded,
        backupInterval,
        showBackupIntervalSelection
    ).map { (
                backupAccount,
                isAccountAdded,
                backupInterval,
                showBackupIntervalSelection
            ) ->
        BackupSettingsState(
            accountEmail = backupAccount,
            isAccountAdded = isAccountAdded,
            showBackupIntervalSelection = showBackupIntervalSelection,
            interval = backupInterval
        )
    }.asStateFlow(viewModelScope, BackupSettingsState())

    val events = eventBus.eventFlow

    init {
        onInit()
//        restoreWorker()
    }

    private fun onInit() {
        backupAccount.update {
            signInService.getSignedInAccount()?.email
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

        val token = tryOrNull { signInService.getAccessToken() }
        println("AppDebug: Token - $token")

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
            preferencesManager.updateAppBackupInterval(interval)

            if (signInService.getSignedInAccount() == null) return@launch

//            backupWorkManager.schedulePeriodicWorker(interval)
        }
    }

    override fun onBackupIntervalSelectionDismiss() {
        savedStateHandle[SHOW_BACKUP_INTERVAL_SELECTION] = false
    }

    override fun onBackupNowClick() {
        viewModelScope.launch {
            backupWorkManager.runBackupWorkerOnceNow()
        }
    }

    override fun onRestoreClick() {
        viewModelScope.launch {
            backupWorkManager.runRestoreWorkerNow()
        }
    }

    private fun restoreWorker() = viewModelScope.launch {
//        backupWorkManager.getRestoreWorkInfoLiveData().asFlow().collectLatest { info ->
//            log { "Restore Worker - ${info.state}" }
//        }
    }

    sealed class BackupEvent {
        data class ShowUiMessage(val uiText: UiText) : BackupEvent()
        data class LaunchGoogleSignIn(val intent: Intent) : BackupEvent()
    }
}

private const val SHOW_BACKUP_INTERVAL_SELECTION = "SHOW_BACKUP_INTERVAL_SELECTION"