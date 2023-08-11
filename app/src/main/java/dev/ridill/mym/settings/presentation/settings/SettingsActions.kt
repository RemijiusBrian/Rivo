package dev.ridill.mym.settings.presentation.settings

import dev.ridill.mym.settings.domain.modal.AppTheme

interface SettingsActions {
    fun onAppThemePreferenceClick()
    fun onAppThemeSelectionDismiss()
    fun onAppThemeSelectionConfirm(appTheme: AppTheme)
    fun onDynamicThemeEnabledChange(enabled: Boolean)
    fun onMonthlyLimitPreferenceClick()
    fun onMonthlyLimitInputDismiss()
    fun onMonthlyLimitInputConfirm(value: String)
    fun onAutoAddExpensePreferenceClick()
    fun onSmsPermissionRationaleDismiss()
    fun onSmsPermissionRationaleAgree()
    fun onFeedbackPreferenceClick()
    fun onBackupAccountClick()
    fun onBackupClick()
    fun onRestoreClick()
}