package dev.ridill.rivo.application

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.ridill.rivo.core.data.preferences.PreferencesManager
import dev.ridill.rivo.core.domain.service.ReceiverService
import dev.ridill.rivo.core.domain.util.EventBus
import dev.ridill.rivo.core.domain.util.asStateFlow
import dev.ridill.rivo.settings.presentation.security.AppLockManager
import dev.ridill.rivo.transactions.domain.sms.SMSModelDownloadManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

@HiltViewModel
class RivoViewModel @Inject constructor(
    private val preferencesManager: PreferencesManager,
    private val receiverService: ReceiverService,
    private val smsModelDownloadManager: SMSModelDownloadManager,
    private val appLockManager: AppLockManager,
    private val eventBus: EventBus<RivoEvent>
) : ViewModel() {
    private val preferences = preferencesManager.preferences
    val showWelcomeFlow = preferences.map { it.showAppWelcomeFlow }
        .distinctUntilChanged()
        .asStateFlow(viewModelScope, true)
    val appTheme = preferences.map { it.appTheme }
        .distinctUntilChanged()
    val dynamicThemeEnabled = preferences.map { it.dynamicColorsEnabled }
        .distinctUntilChanged()
    val isAppLocked = preferences.map { it.isAppLocked }
        .distinctUntilChanged()

    val showSplashScreen = MutableStateFlow(true)

    val events = eventBus.eventFlow

    init {
        toggleSplashScreenVisibility()
        collectTransactionAutoAdd()
        collectAppLockEnabled()
    }

    private fun toggleSplashScreenVisibility() = viewModelScope.launch {
        showWelcomeFlow.collect {
            delay(SPLASH_SCREEN_DELAY_SECONDS.seconds)
            showSplashScreen.update { false }
        }
    }

    private fun collectTransactionAutoAdd() = viewModelScope.launch {
        preferences.map { it.autoAddTransactionEnabled }
            .collectLatest { enabled ->
                receiverService.toggleSmsReceiver(enabled)
                if (enabled)
                    smsModelDownloadManager.downloadSMSModelIfNeeded()
            }
    }

    fun onSmsPermissionCheck(granted: Boolean) = viewModelScope.launch {
        preferencesManager.updateAutoAddTransactionEnabled(granted)
    }

    fun onNotificationPermissionCheck(granted: Boolean) {
        receiverService.toggleNotificationActionReceivers(granted)
    }

    private fun collectAppLockEnabled() = viewModelScope.launch {
        preferences.map { it.appLockEnabled }
            .collectLatest { enabled ->
                eventBus.send(RivoEvent.EnableSecureFlags(enabled))
            }
    }

    fun startAppAutoLockTimer() = viewModelScope.launch {
        val preferences = preferences.first()
        if (!preferences.appLockEnabled || preferences.isAppLocked) return@launch
        appLockManager.startAppLockTimerService()
    }

    fun stopAppAutoLockTimer() {
        appLockManager.stopAppLockTimer()
    }

    fun onAppLockAuthSucceeded() = viewModelScope.launch {
        preferencesManager.updateAppLocked(false)
    }

    sealed class RivoEvent {
        data class EnableSecureFlags(val enabled: Boolean) : RivoEvent()
    }
}

private const val SPLASH_SCREEN_DELAY_SECONDS = 1