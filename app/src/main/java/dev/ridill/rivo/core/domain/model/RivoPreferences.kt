package dev.ridill.rivo.core.domain.model

import dev.ridill.rivo.settings.domain.modal.AppTheme
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