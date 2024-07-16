package dev.ridill.rivo.settings.presentation.settings

import dev.ridill.rivo.settings.domain.modal.AppTheme
import java.util.Currency

interface SettingsActions {
    fun onLoginOrLogoutPreferenceClick()
    fun onLogoutDismiss()
    fun onLogoutConfirm()
    fun onAppThemePreferenceClick()
    fun onAppThemeSelectionDismiss()
    fun onAppThemeSelectionConfirm(appTheme: AppTheme)
    fun onDynamicThemeEnabledChange(enabled: Boolean)
    fun onMonthlyBudgetPreferenceClick()
    fun onMonthlyBudgetInputDismiss()
    fun onMonthlyBudgetInputConfirm(value: String)
    fun onCurrencyPreferenceClick()
    fun onCurrencySelectionDismiss()
    fun onCurrencySelectionConfirm(currency: Currency)
    fun onCurrencySearchQueryChange(value: String)
    fun onToggleAutoAddTransactions(enabled: Boolean)
    fun onSmsPermissionRationaleDismiss()
    fun onSmsPermissionRationaleSettingsClick()
}