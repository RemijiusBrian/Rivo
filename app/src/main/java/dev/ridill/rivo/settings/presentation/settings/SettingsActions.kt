package dev.ridill.rivo.settings.presentation.settings

import dev.ridill.rivo.settings.domain.modal.AppTheme

interface SettingsActions {
    fun onAppThemePreferenceClick()
    fun onAppThemeSelectionDismiss()
    fun onAppThemeSelectionConfirm(appTheme: AppTheme)
    fun onDynamicThemeEnabledChange(enabled: Boolean)
    fun onToggleAutoAddTransactions(enabled: Boolean)
    fun onAutoDetectTxFeatureInfoDismiss()
    fun onAutoDetectTxFeatureInfoAcknowledge()
    fun onSmsPermissionRationaleDismiss()
    fun onSmsPermissionRationaleSettingsClick()
}