package dev.ridill.rivo.core.data.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import dev.ridill.rivo.core.domain.model.RivoPreferences
import dev.ridill.rivo.core.domain.util.DateUtil
import dev.ridill.rivo.settings.domain.modal.AppTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.time.LocalDateTime

class PreferencesManagerImpl(
    private val dataStore: DataStore<Preferences>
) : PreferencesManager {

    override val preferences: Flow<RivoPreferences> = dataStore.data
        .map { preferences ->
            val showAppWelcomeFlow = preferences[Keys.SHOW_WELCOME_FLOW] ?: true
            val appTheme = AppTheme.valueOf(
                preferences[Keys.APP_THEME] ?: AppTheme.SYSTEM_DEFAULT.name
            )
            val dynamicColorsEnabled = preferences[Keys.DYNAMIC_COLORS_ENABLED] ?: false
            val lastBackupDateTime = preferences[Keys.LAST_BACKUP_TIMESTAMP]
                ?.let { DateUtil.parse(it) }
            val needsConfigRestore = preferences[Keys.NEEDS_CONFIG_RESTORE] ?: false
            val autoAddTransactionEnabled = preferences[Keys.AUTO_ADD_TRANSACTION_ENABLED] ?: false
            val showExcludedTransactions = preferences[Keys.SHOW_EXCLUDED_TRANSACTIONS] ?: true

            RivoPreferences(
                showAppWelcomeFlow = showAppWelcomeFlow,
                appTheme = appTheme,
                dynamicColorsEnabled = dynamicColorsEnabled,
                lastBackupDateTime = lastBackupDateTime,
                needsConfigRestore = needsConfigRestore,
                autoAddTransactionEnabled = autoAddTransactionEnabled,
                showExcludedTransactions = showExcludedTransactions
            )
        }

    override suspend fun concludeWelcomeFlow() {
        withContext(Dispatchers.IO) {
            dataStore.edit { preferences ->
                preferences[Keys.SHOW_WELCOME_FLOW] = false
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

    override suspend fun updateLastBackupTimestamp(localDateTime: LocalDateTime) {
        withContext(Dispatchers.IO) {
            dataStore.edit { preferences ->
                preferences[Keys.LAST_BACKUP_TIMESTAMP] = localDateTime.toString()
            }
        }
    }

    override suspend fun updateNeedsConfigRestore(needsRestore: Boolean) {
        withContext(Dispatchers.IO) {
            dataStore.edit { preferences ->
                preferences[Keys.NEEDS_CONFIG_RESTORE] = needsRestore
            }
        }
    }

    override suspend fun updateAutoAddTransactionEnabled(enabled: Boolean) {
        withContext(Dispatchers.IO) {
            dataStore.edit { preferences ->
                preferences[Keys.AUTO_ADD_TRANSACTION_ENABLED] = enabled
            }
        }
    }

    override suspend fun updateShowExcludedTransactions(show: Boolean) {
        withContext(Dispatchers.IO) {
            dataStore.edit { preferences ->
                preferences[Keys.SHOW_EXCLUDED_TRANSACTIONS] = show
            }
        }
    }

    private object Keys {
        val SHOW_WELCOME_FLOW = booleanPreferencesKey("SHOW_WELCOME_FLOW")
        val APP_THEME = stringPreferencesKey("APP_THEME")
        val DYNAMIC_COLORS_ENABLED = booleanPreferencesKey("DYNAMIC_COLORS_ENABLED")
        val LAST_BACKUP_TIMESTAMP = stringPreferencesKey("LAST_BACKUP_TIMESTAMP")
        val NEEDS_CONFIG_RESTORE = booleanPreferencesKey("NEEDS_CONFIG_RESTORE")
        val AUTO_ADD_TRANSACTION_ENABLED = booleanPreferencesKey("AUTO_ADD_TRANSACTION_ENABLED")
        val SHOW_EXCLUDED_TRANSACTIONS = booleanPreferencesKey("SHOW_EXCLUDED_TRANSACTIONS")
    }
}