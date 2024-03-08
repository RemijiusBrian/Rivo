package dev.ridill.rivo.settings.presentation.security

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.ridill.rivo.core.data.preferences.PreferencesManager
import dev.ridill.rivo.core.domain.util.EventBus
import dev.ridill.rivo.settings.domain.appLock.AppAutoLockInterval
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SecuritySettingsViewModel @Inject constructor(
    private val preferencesManager: PreferencesManager,
    private val eventBus: EventBus<SecuritySettingsEvent>
) : ViewModel() {

    private val preferences = preferencesManager.preferences
    val appLockEnabled = preferences
        .map { it.appLockEnabled }
        .distinctUntilChanged()
    val appAutoLockInterval = preferences
        .map { it.appAutoLockInterval }
        .distinctUntilChanged()

    val screenSecurityEnabled = preferences
        .map { it.screenSecurityEnabled }
        .distinctUntilChanged()

    val events = eventBus.eventFlow

    fun onAppLockToggle(enabled: Boolean) {
        viewModelScope.launch {
            if (!enabled) {
                preferencesManager.updateAppLockEnabled(false)
                return@launch
            }
            eventBus.send(SecuritySettingsEvent.LaunchAuthentication)
        }
    }

    fun onAuthenticationSuccess() {
        viewModelScope.launch {
            preferencesManager.updateAppLockEnabled(true)
        }
    }

    fun onAutoLockIntervalSelect(interval: AppAutoLockInterval) {
        viewModelScope.launch {
            preferencesManager.updateAppAutoLockInterval(interval)
        }
    }

    fun onScreenSecurityToggle(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.updateScreenSecurityEnabled(enabled)
        }
    }

    sealed interface SecuritySettingsEvent {
        data object LaunchAuthentication : SecuritySettingsEvent
    }
}