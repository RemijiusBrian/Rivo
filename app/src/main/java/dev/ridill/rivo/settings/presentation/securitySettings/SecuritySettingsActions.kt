package dev.ridill.rivo.settings.presentation.securitySettings

import dev.ridill.rivo.settings.domain.appLock.AppAutoLockInterval

interface SecuritySettingsActions {
    fun onAppLockToggle(enabled: Boolean)
    fun onAutoLockIntervalSelect(interval: AppAutoLockInterval)
    fun onScreenSecurityToggle(enabled: Boolean)
    fun onNotificationPermissionRationaleDismiss()
    fun onNotificationPermissionRationaleConfirm()
}