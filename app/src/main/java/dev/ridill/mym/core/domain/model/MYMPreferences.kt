package dev.ridill.mym.core.domain.model

import dev.ridill.mym.settings.domain.modal.AppTheme
import dev.ridill.mym.settings.domain.modal.BackupInterval
import java.time.LocalDateTime

data class MYMPreferences(
    val showAppWelcomeFlow: Boolean,
    val appTheme: AppTheme,
    val dynamicColorsEnabled: Boolean,
    val appBackupInterval: BackupInterval,
    val lastBackupDateTime: LocalDateTime?
)