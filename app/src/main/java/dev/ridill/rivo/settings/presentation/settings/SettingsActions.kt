package dev.ridill.rivo.settings.presentation.settings

import dev.ridill.rivo.settings.domain.modal.AppTheme

interface SettingsActions {
    fun onAppThemePreferenceClick()
    fun onAppThemeSelectionDismiss()
    fun onAppThemeSelectionConfirm(appTheme: AppTheme)
    fun onDynamicThemeEnabledChange(enabled: Boolean)
    fun onMonthlyBudgetPreferenceClick()
    fun onMonthlyBudgetInputDismiss()
    fun onMonthlyBudgetInputConfirm(value: String)
    fun onCurrencyPreferenceClick()
    fun onCurrencySelectionDismiss()
    fun onCurrencySelectionConfirm(currencyCode: String)
    fun onCurrencySearchQueryChange(value: String)
    fun onToggleAutoAddExpense(enabled: Boolean)
    fun onSmsPermissionRationaleDismiss()
    fun onSmsPermissionRationaleSettingsClick()
}