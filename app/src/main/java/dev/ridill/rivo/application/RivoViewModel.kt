package dev.ridill.rivo.application

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.ridill.rivo.core.data.preferences.PreferencesManager
import dev.ridill.rivo.core.domain.service.ReceiverService
import dev.ridill.rivo.core.domain.util.EventBus
import dev.ridill.rivo.core.domain.util.asStateFlow
import dev.ridill.rivo.core.ui.util.UiText
import dev.ridill.rivo.settings.domain.appLock.AppLockServiceManager
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
class RivoViewModel @Inject constructor(
    private val preferencesManager: PreferencesManager,
    private val receiverService: ReceiverService,
    private val appLockServiceManager: AppLockServiceManager,
    private val eventBus: EventBus<RivoEvent>,
    private val backupSettingsRepo: BackupSettingsRepository
) : ViewModel() {
    private val preferences = preferencesManager.preferences
    val showWelcomeFlow = preferences.map { it.showOnboarding }
        .distinctUntilChanged()
        .asStateFlow(viewModelScope, true)
    val appTheme = preferences.map { it.appTheme }
        .distinctUntilChanged()
    val dynamicThemeEnabled = preferences.map { it.dynamicColorsEnabled }
        .distinctUntilChanged()
    val isAppLocked = preferences.map { it.isAppLocked }
        .distinctUntilChanged()
    val appLockAuthErrorMessage = MutableStateFlow<UiText?>(null)

    val screenSecurityEnabled = preferences.map { it.screenSecurityEnabled }
        .distinctUntilChanged()

    val events = eventBus.eventFlow

    init {
        collectTransactionAutoAdd()
        collectConfigRestore()
        collectIsAppLocked()
    }

    private fun collectTransactionAutoAdd() = viewModelScope.launch {
        preferences.map { it.autoAddTransactionEnabled }
            .collectLatest { enabled ->
                receiverService.toggleSmsReceiver(enabled)
            }
    }

    private fun collectConfigRestore() = viewModelScope.launch {
        preferencesManager.preferences.map { it.needsConfigRestore }
            .collectLatest { needsRestore ->
                if (!needsRestore) return@collectLatest
                backupSettingsRepo.restoreBackupJob()
                preferencesManager.updateNeedsConfigRestore(false)
            }
    }

    fun onSmsPermissionCheck(granted: Boolean) = viewModelScope.launch {
        if (!granted) {
            preferencesManager.updateAutoAddTransactionEnabled(false)
        }
    }

    fun onNotificationPermissionCheck(granted: Boolean) {
        receiverService.toggleNotificationActionReceivers(granted)
    }

    private fun collectIsAppLocked() = viewModelScope.launch {
        preferences
            .map { Pair(it.appLockEnabled, it.isAppLocked) }
            .collectLatest { (appLockedEnabled, isLocked) ->
                if (!appLockedEnabled || isLocked) {
                    appLockServiceManager.stopAppUnlockedIndicator()
                } else {
                    appLockServiceManager.startAppUnlockedIndicator()
                }
            }
    }

    fun runAppLockProcess() = viewModelScope.launch {
        val preferences = preferences.first()
        if (!preferences.appLockEnabled || preferences.isAppLocked) return@launch
        appLockServiceManager.startAppAutoLockTimer()
    }

    fun runAppUnlockAppProcess() = viewModelScope.launch {
        startAppUnlockOrServiceStop()
    }

    private suspend fun startAppUnlockOrServiceStop() {
        val preferences = preferences.first()
        if (!preferences.appLockEnabled) return
        if (preferences.isAppLocked) {
            eventBus.send(RivoEvent.LaunchAppLockAuthentication)
        } else {
            appLockServiceManager.stopAppLockTimer()
        }
    }

    fun onAppLockAuthSucceeded() = viewModelScope.launch {
        preferencesManager.updateAppLocked(false)
    }

    fun updateAppLockErrorMessage(message: UiText?) {
        appLockAuthErrorMessage.update { message }
    }

    sealed class RivoEvent {
        data object LaunchAppLockAuthentication : RivoEvent()
    }
}