package dev.ridill.mym.core.domain.model

import dev.ridill.mym.settings.domain.modal.AppTheme

data class MYMPreferences(
    val isAppFirstLaunch: Boolean,
    val monthlyLimit: Long,
    val appTheme: AppTheme,
    val dynamicColorsEnabled: Boolean
)