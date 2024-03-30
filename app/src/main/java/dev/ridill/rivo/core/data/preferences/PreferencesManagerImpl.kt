package dev.ridill.rivo.core.data.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import dev.ridill.rivo.core.domain.model.RivoPreferences
import dev.ridill.rivo.core.domain.util.DateUtil
import dev.ridill.rivo.core.domain.util.logE
import dev.ridill.rivo.core.domain.util.orFalse
import dev.ridill.rivo.core.domain.util.orTrue
import dev.ridill.rivo.core.domain.util.tryOrNull
import dev.ridill.rivo.settings.domain.appLock.AppAutoLockInterval
import dev.ridill.rivo.settings.domain.modal.AppTheme
import dev.ridill.rivo.settings.domain.repositoty.FatalBackupError
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.IOException
import java.time.LocalDateTime

class PreferencesManagerImpl(
    private val dataStore: DataStore<Preferences>
) : PreferencesManager {

    override val preferences: Flow<RivoPreferences> = dataStore.data
        .catch { cause ->
            if (cause is IOException) {
                logE(cause) { "Preferences Exception" }
                emit(emptyPreferences())
            } else throw cause
        }
        .map { preferences ->
            val showOnboarding = preferences[Keys.SHOW_ONBOARDING] ?: true
            val appTheme = AppTheme.valueOf(
                preferences[Keys.APP_THEME] ?: AppTheme.SYSTEM_DEFAULT.name
            )
            val dynamicColorsEnabled = preferences[Keys.DYNAMIC_COLORS_ENABLED].orFalse()
            val lastBackupDateTime = preferences[Keys.LAST_BACKUP_TIMESTAMP]
                ?.let { DateUtil.parseDateTime(it) }
            val needsConfigRestore = preferences[Keys.NEEDS_CONFIG_RESTORE].orFalse()
            val autoAddTransactionEnabled = preferences[Keys.AUTO_ADD_TRANSACTION_ENABLED].orFalse()
            val allTransactionsShowExcludedOption =
                preferences[Keys.ALL_TX_SHOW_EXCLUDED_OPTION].orTrue()
            val appLockEnabled = preferences[Keys.APP_LOCK_ENABLED].orFalse()
            val appAutoLockInterval = AppAutoLockInterval.valueOf(
                preferences[Keys.APP_AUTO_LOCK_INTERVAL] ?: AppAutoLockInterval.ONE_MINUTE.name
            )
            val isAppLocked = preferences[Keys.IS_APP_LOCKED].orFalse()
            val screenSecurityEnabled = preferences[Keys.SCREEN_SECURITY_ENABLED].orFalse()
            val encryptionPasswordHash = preferences[Keys.ENCRYPTION_PASSWORD_HASH]
            val fatalBackupError = tryOrNull {
                preferences[Keys.FATAL_BACKUP_ERROR]?.let { FatalBackupError.valueOf(it) }
            }

            RivoPreferences(
                showOnboarding = showOnboarding,
                appTheme = appTheme,
                dynamicColorsEnabled = dynamicColorsEnabled,
                lastBackupDateTime = lastBackupDateTime,
                needsConfigRestore = needsConfigRestore,
                autoAddTransactionEnabled = autoAddTransactionEnabled,
                allTransactionsShowExcludedOption = allTransactionsShowExcludedOption,
                appLockEnabled = appLockEnabled,
                appAutoLockInterval = appAutoLockInterval,
                isAppLocked = isAppLocked,
                screenSecurityEnabled = screenSecurityEnabled,
                encryptionPasswordHash = encryptionPasswordHash,
                fatalBackupError = fatalBackupError
            )
        }

    override suspend fun concludeOnboarding() {
        withContext(Dispatchers.IO) {
            dataStore.edit { preferences ->
                preferences[Keys.SHOW_ONBOARDING] = false
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

    override suspend fun updateAllTransactionsShowExcludedOption(show: Boolean) {
        withContext(Dispatchers.IO) {
            dataStore.edit { preferences ->
                preferences[Keys.ALL_TX_SHOW_EXCLUDED_OPTION] = show
            }
        }
    }

    override suspend fun updateAppLockEnabled(enabled: Boolean) {
        withContext(Dispatchers.IO) {
            dataStore.edit { preferences ->
                preferences[Keys.APP_LOCK_ENABLED] = enabled
            }
        }
    }

    override suspend fun updateAppAutoLockInterval(interval: AppAutoLockInterval) {
        withContext(Dispatchers.IO) {
            dataStore.edit { preferences ->
                preferences[Keys.APP_AUTO_LOCK_INTERVAL] = interval.name
            }
        }
    }

    override suspend fun updateAppLocked(locked: Boolean) {
        withContext(Dispatchers.IO) {
            dataStore.edit { preferences ->
                preferences[Keys.IS_APP_LOCKED] = locked
            }
        }
    }

    override suspend fun updateScreenSecurityEnabled(enabled: Boolean) {
        withContext(Dispatchers.IO) {
            dataStore.edit { preferences ->
                preferences[Keys.SCREEN_SECURITY_ENABLED] = enabled
            }
        }
    }

    override suspend fun updateEncryptionPasswordHash(hash: String?) {
        withContext(Dispatchers.IO) {
            dataStore.edit { preferences ->
                preferences[Keys.ENCRYPTION_PASSWORD_HASH] = hash.orEmpty()
            }
        }
    }

    override suspend fun updateFatalBackupError(error: FatalBackupError?) {
        withContext(Dispatchers.IO) {
            dataStore.edit { preferences ->
                preferences[Keys.FATAL_BACKUP_ERROR] = error?.name.orEmpty()
            }
        }
    }

    private object Keys {
        val SHOW_ONBOARDING = booleanPreferencesKey("SHOW_ONBOARDING")
        val APP_THEME = stringPreferencesKey("APP_THEME")
        val DYNAMIC_COLORS_ENABLED = booleanPreferencesKey("DYNAMIC_COLORS_ENABLED")
        val LAST_BACKUP_TIMESTAMP = stringPreferencesKey("LAST_BACKUP_TIMESTAMP")
        val NEEDS_CONFIG_RESTORE = booleanPreferencesKey("NEEDS_CONFIG_RESTORE")
        val AUTO_ADD_TRANSACTION_ENABLED = booleanPreferencesKey("AUTO_ADD_TRANSACTION_ENABLED")
        val ALL_TX_SHOW_EXCLUDED_OPTION = booleanPreferencesKey("ALL_TX_SHOW_EXCLUDED_OPTION")
        val APP_LOCK_ENABLED = booleanPreferencesKey("APP_LOCK_ENABLED")
        val APP_AUTO_LOCK_INTERVAL = stringPreferencesKey("APP_AUTO_LOCK_INTERVAL")
        val IS_APP_LOCKED = booleanPreferencesKey("IS_APP_LOCKED")
        val SCREEN_SECURITY_ENABLED = booleanPreferencesKey("SCREEN_SECURITY_ENABLED")
        val ENCRYPTION_PASSWORD_HASH = stringPreferencesKey("ENCRYPTION_PASSWORD_HASH")
        val FATAL_BACKUP_ERROR = stringPreferencesKey("FATAL_BACKUP_ERROR")
    }
}