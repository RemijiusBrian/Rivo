package dev.ridill.rivo.settings.domain.repositoty

import kotlinx.coroutines.flow.Flow
import java.util.Currency

interface SettingsRepository {
    fun getCurrentBudget(): Flow<Long>
    suspend fun updateCurrentBudget(value: Long)
    fun getCurrencyPreference(): Flow<Currency>
    suspend fun updateCurrency(currency: Currency)
}