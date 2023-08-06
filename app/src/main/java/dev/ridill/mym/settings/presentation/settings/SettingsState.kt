package dev.ridill.mym.settings.presentation.settings

import dev.ridill.mym.settings.domain.modal.AppTheme

data class SettingsState(
    val appTheme: AppTheme = AppTheme.SYSTEM_DEFAULT,
    val dynamicColorsEnabled: Boolean = false,
    val showAppThemeSelection: Boolean = false,
    val currentMonthlyLimit: String = "",
    val showMonthlyLimitInput: Boolean = false,
    val showSmsPermissionRationale: Boolean = false
)