package dev.ridill.mym.core.data.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import dev.ridill.mym.core.domain.model.MYMPreferences
import dev.ridill.mym.core.domain.util.DateUtil
import dev.ridill.mym.core.domain.util.Zero
import dev.ridill.mym.settings.domain.modal.AppTheme
import dev.ridill.mym.settings.domain.modal.BackupInterval
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.time.LocalDateTime

class PreferencesManagerImpl(
    private val dataStore: DataStore<Preferences>
) : PreferencesManager {

    override val preferences: Flow<MYMPreferences> = dataStore.data
        .map { preferences ->
            val showAppWelcomeFlow = preferences[Keys.SHOW_WELCOME_FLOW] ?: true
            val monthlyLimit = preferences[Keys.MONTHLY_LIMIT] ?: Long.Zero
            val appTheme = AppTheme.valueOf(
                preferences[Keys.APP_THEME] ?: AppTheme.SYSTEM_DEFAULT.name
            )
            val dynamicColorsEnabled = preferences[Keys.DYNAMIC_COLORS_ENABLED] ?: false
            val appBackupInterval = BackupInterval.valueOf(
                preferences[Keys.APP_BACKUP_INTERVAL] ?: BackupInterval.NEVER.name
            )
            val lastBackupDateTime = preferences[Keys.LAST_BACKUP_TIMESTAMP]
                ?.let { DateUtil.parse(it) }

            MYMPreferences(
                showAppWelcomeFlow = showAppWelcomeFlow,
                monthlyLimit = monthlyLimit,
                appTheme = appTheme,
                dynamicColorsEnabled = dynamicColorsEnabled,
                appBackupInterval = appBackupInterval,
                lastBackupDateTime = lastBackupDateTime
            )
        }

    override suspend fun concludeWelcomeFlow() {
        withContext(Dispatchers.IO) {
            dataStore.edit { preferences ->
                preferences[Keys.SHOW_WELCOME_FLOW] = false
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

    override suspend fun updateAppThem(theme: AppTheme) {
        withContext(Dispatchers.IO) {
            dataStore.edit { preferences ->
                preferences[Keys.APP_THEME] = theme.name
            }
        }
    }

    override suspend fun updateDynamicColorsEnabled(enabled: Boolean) {
        withContext(Dispatchers.IO) {
            dataStore.edit { preferences ->
                preferences[Keys.DYNAMIC_COLORS_ENABLED] = enabled
            }
        }
    }

    override suspend fun updateAppBackupInterval(interval: BackupInterval) {
        withContext(Dispatchers.IO) {
            dataStore.edit { preferences ->
                preferences[Keys.APP_BACKUP_INTERVAL] = interval.name
            }
        }
    }

    override suspend fun updateLastBackupTimestamp(localDateTime: LocalDateTime) {
        withContext(Dispatchers.IO) {
            dataStore.edit { preferences ->
                preferences[Keys.LAST_BACKUP_TIMESTAMP] = localDateTime.toString()
            }
        }
    }

    private object Keys {
        val SHOW_WELCOME_FLOW = booleanPreferencesKey("SHOW_WELCOME_FLOW")
        val MONTHLY_LIMIT = longPreferencesKey("MONTHLY_LIMIT")
        val APP_THEME = stringPreferencesKey("APP_THEME")
        val DYNAMIC_COLORS_ENABLED = booleanPreferencesKey("DYNAMIC_COLORS_ENABLED")
        val APP_BACKUP_INTERVAL = stringPreferencesKey("APP_BACKUP_INTERVAL")
        val LAST_BACKUP_TIMESTAMP = stringPreferencesKey("LAST_BACKUP_TIMESTAMP")
    }
}