package dev.ridill.mym.core.domain.model

import dev.ridill.mym.core.ui.util.UiText
import dev.ridill.mym.settings.domain.modal.AppTheme
import java.time.LocalDateTime

data class MYMPreferences(
    val showAppWelcomeFlow: Boolean,
    val appTheme: AppTheme,
    val dynamicColorsEnabled: Boolean,
    val lastBackupDateTime: LocalDateTime?,
    val needsConfigRestore: Boolean,
    val backupWorkerMessage: UiText?,
    val autoAddExpenseEnabled: Boolean
)