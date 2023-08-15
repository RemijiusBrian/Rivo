package dev.ridill.mym.core.data.preferences

import dev.ridill.mym.core.domain.model.MYMPreferences
import dev.ridill.mym.settings.domain.modal.AppTheme
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

interface PreferencesManager {
    companion object {
        const val NAME = "MYM_preferences"
    }

    val preferences: Flow<MYMPreferences>

    suspend fun concludeWelcomeFlow()
    suspend fun updateAppThem(theme: AppTheme)
    suspend fun updateDynamicColorsEnabled(enabled: Boolean)
    suspend fun updateLastBackupTimestamp(localDateTime: LocalDateTime)
}