package dev.ridill.rivo.settings.presentation.settings

import dev.ridill.rivo.settings.domain.modal.AppTheme
import java.util.Currency

interface SettingsActions {
    fun onAppThemePreferenceClick()
    fun onAppThemeSelectionDismiss()
    fun onAppThemeSelectionConfirm(appTheme: AppTheme)
    fun onDynamicThemeEnabledChange(enabled: Boolean)
    fun onCurrencyPreferenceClick()
    fun onCurrencySelectionDismiss()
    fun onCurrencySelectionConfirm(currency: Currency)
    fun onCurrencySearchQueryChange(value: String)
    fun onToggleAutoAddTransactions(enabled: Boolean)
    fun onAutoDetectTxFeatureInfoDismiss()
    fun onAutoDetectTxFeatureInfoAcknowledge()
    fun onSmsPermissionRationaleDismiss()
    fun onSmsPermissionRationaleSettingsClick()
}