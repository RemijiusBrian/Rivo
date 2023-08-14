package dev.ridill.mym.settings.presentation.settings

import dev.ridill.mym.settings.domain.modal.AppTheme

interface SettingsActions {
    fun onAppThemePreferenceClick()
    fun onAppThemeSelectionDismiss()
    fun onAppThemeSelectionConfirm(appTheme: AppTheme)
    fun onDynamicThemeEnabledChange(enabled: Boolean)
    fun onMonthlyBudgetPreferenceClick()
    fun onMonthlyBudgetInputDismiss()
    fun onMonthlyBudgetInputConfirm(value: String)
    fun onAutoAddExpensePreferenceClick()
    fun onSmsPermissionRationaleDismiss()
    fun onSmsPermissionRationaleAgree()
}