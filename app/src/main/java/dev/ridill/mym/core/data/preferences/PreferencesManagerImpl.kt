package dev.ridill.mym.core.data.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import dev.ridill.mym.core.domain.model.MYMPreferences
import dev.ridill.mym.core.domain.util.Zero
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class PreferencesManagerImpl(
    private val dataStore: DataStore<Preferences>
) : PreferencesManager {

    override val preferences: Flow<MYMPreferences> = dataStore.data
        .map { preferences ->
            val monthlyLimit = preferences[Keys.MONTHLY_LIMIT] ?: Long.Zero

            MYMPreferences(
                monthlyLimit = monthlyLimit
            )
        }

    override suspend fun updateMonthlyLimit(value: Long) {
        withContext(Dispatchers.IO) {
            dataStore.edit { preferences ->
                preferences[Keys.MONTHLY_LIMIT] = value
            }
        }
    }

    private object Keys {
        val MONTHLY_LIMIT = longPreferencesKey("MONTHLY_LIMIT")
    }
}