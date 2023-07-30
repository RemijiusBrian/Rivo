package dev.ridill.mym.settings.presentation.settings

import dev.ridill.mym.settings.domain.modal.AppTheme

data class SettingsState(
    val appTheme: AppTheme = AppTheme.SYSTEM_DEFAULT,
    val dynamicThemeEnabled: Boolean = false,
    val showAppThemeSelection: Boolean = false
)