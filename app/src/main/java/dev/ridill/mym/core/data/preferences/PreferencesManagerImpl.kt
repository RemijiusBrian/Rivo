package dev.ridill.mym.core.data.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
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
            val isAppFirstLaunch = preferences[Keys.IS_APP_FIRST_LAUNCH] ?: true
            val monthlyLimit = preferences[Keys.MONTHLY_LIMIT] ?: Long.Zero

            MYMPreferences(
                isAppFirstLaunch = isAppFirstLaunch,
                monthlyLimit = monthlyLimit
            )
        }

    override suspend fun disableAppFirstLaunch() {
        withContext(Dispatchers.IO) {
            dataStore.edit { preferences ->
                preferences[Keys.IS_APP_FIRST_LAUNCH] = false
            }
        }
    }

    override suspend fun updateMonthlyLimit(value: Long) {
        withContext(Dispatchers.IO) {
            dataStore.edit { preferences ->
                preferences[Keys.MONTHLY_LIMIT] = value
            }
        }
    }

    private object Keys {
        val IS_APP_FIRST_LAUNCH = booleanPreferencesKey("IS_APP_FIRST_LAUNCH")
        val MONTHLY_LIMIT = longPreferencesKey("MONTHLY_LIMIT")
    }
}