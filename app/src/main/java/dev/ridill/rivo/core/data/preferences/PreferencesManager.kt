package dev.ridill.rivo.core.data.preferences

import dev.ridill.rivo.core.domain.model.RivoPreferences
import dev.ridill.rivo.settings.domain.modal.AppTheme
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

interface PreferencesManager {
    companion object {
        const val NAME = "MYM_preferences"
    }

    val preferences: Flow<RivoPreferences>

    suspend fun concludeWelcomeFlow()
    suspend fun updateAppThem(theme: AppTheme)
    suspend fun updateDynamicColorsEnabled(enabled: Boolean)
    suspend fun updateLastBackupTimestamp(localDateTime: LocalDateTime)
    suspend fun updateNeedsConfigRestore(needsRestore: Boolean)
    suspend fun updateAutoAddExpenseEnabled(enabled: Boolean)
    suspend fun updateShowExcludedExpenses(show: Boolean)
}