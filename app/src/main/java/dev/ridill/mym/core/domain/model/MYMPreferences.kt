package dev.ridill.mym.core.domain.model

import dev.ridill.mym.settings.domain.modal.AppTheme
import dev.ridill.mym.settings.domain.modal.BackupInterval

data class MYMPreferences(
    val showAppWelcomeFlow: Boolean,
    val monthlyLimit: Long,
    val appTheme: AppTheme,
    val dynamicColorsEnabled: Boolean,
    val appBackupInterval: BackupInterval
)