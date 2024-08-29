package dev.ridill.rivo.settings.domain.repositoty

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import java.util.Currency

interface SettingsRepository {
    fun getCurrentBudget(): Flow<Long>
    fun getCurrenciesListPaged(query: String): Flow<PagingData<Currency>>
    fun getCurrencyPreference(): Flow<Currency>
    suspend fun updateCurrency(currency: Currency)
}