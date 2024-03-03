package dev.ridill.rivo.settings.domain.repositoty

import android.icu.util.Currency
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    fun getCurrentBudget(): Flow<Long>
    suspend fun updateCurrentBudget(value: Long)
    fun getCurrencyPreference(): Flow<Currency>
    suspend fun updateCurrency(currency: Currency)
}