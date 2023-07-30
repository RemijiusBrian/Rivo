package dev.ridill.mym.core.data.preferences

import dev.ridill.mym.core.domain.model.MYMPreferences
import dev.ridill.mym.settings.domain.modal.AppTheme
import kotlinx.coroutines.flow.Flow

interface PreferencesManager {
    companion object {
        const val NAME = "MYM_preferences"
    }

    val preferences: Flow<MYMPreferences>

    suspend fun disableAppFirstLaunch()
    suspend fun updateMonthlyLimit(value: Long)
    suspend fun updateAppThem(theme: AppTheme)
    suspend fun updateDynamicThemeEnabled(enabled: Boolean)
}