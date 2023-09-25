package dev.ridill.mym.core.domain.model

import dev.ridill.mym.settings.domain.modal.AppTheme
import java.time.LocalDateTime

data class RivoPreferences(
    val showAppWelcomeFlow: Boolean,
    val appTheme: AppTheme,
    val dynamicColorsEnabled: Boolean,
    val lastBackupDateTime: LocalDateTime?,
    val needsConfigRestore: Boolean,
    val autoAddExpenseEnabled: Boolean,
    val showExcludedExpenses: Boolean
)