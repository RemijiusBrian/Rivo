package dev.ridill.mym.core.data.preferences

import dev.ridill.mym.core.domain.model.MYMPreferences
import kotlinx.coroutines.flow.Flow

interface PreferencesManager {
    companion object {
        const val NAME = "MYM_preferences"
    }

    val preferences: Flow<MYMPreferences>

    suspend fun updateMonthlyLimit(value: Long)
}