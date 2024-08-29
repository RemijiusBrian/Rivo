package dev.ridill.rivo.settings.domain.repositoty

import dev.ridill.rivo.account.domain.model.AuthState
import dev.ridill.rivo.core.domain.model.RivoPreferences
import dev.ridill.rivo.settings.domain.modal.AppTheme
import kotlinx.coroutines.flow.Flow
import java.util.Currency

interface SettingsRepository {
    fun getAuthState(): Flow<AuthState>
    fun getPreferences(): Flow<RivoPreferences>
    suspend fun updateAppTheme(theme: AppTheme)
    suspend fun toggleDynamicColors(enabled: Boolean)
    fun getCurrentBudget(): Flow<Long>
    fun getCurrencyPreference(): Flow<Currency>
    suspend fun toggleShowAutoDetectTxInfoFalse()
    suspend fun toggleAutoDetectTransactions(enabled: Boolean)
}