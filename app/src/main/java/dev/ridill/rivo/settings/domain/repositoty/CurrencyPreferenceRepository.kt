package dev.ridill.rivo.settings.domain.repositoty

import androidx.paging.PagingData
import dev.ridill.rivo.core.domain.util.DateUtil
import dev.ridill.rivo.core.domain.util.Empty
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.util.Currency

interface CurrencyPreferenceRepository {
    fun getCurrencyPreferenceForDateOrNext(
        date: LocalDate = DateUtil.dateNow()
    ): Flow<Currency>

    suspend fun saveCurrencyPreference(
        currency: Currency,
        date: LocalDate = DateUtil.dateNow()
    )

    fun getAllCurrenciesPaged(searchQuery: String = String.Empty): Flow<PagingData<Currency>>
}