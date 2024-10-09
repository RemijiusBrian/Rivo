package dev.ridill.rivo.settings.presentation.securitySettings

import dev.ridill.rivo.settings.domain.appLock.AppAutoLockInterval

data class SecuritySettingsState(
    val appLockEnabled: Boolean = false,
    val autoLockInterval: AppAutoLockInterval = AppAutoLockInterval.ONE_MINUTE,
    val screenSecurityEnabled: Boolean = false,
    val showNotificationPermissionRationale: Boolean = false
)