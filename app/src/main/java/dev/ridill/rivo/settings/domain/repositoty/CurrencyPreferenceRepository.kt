package dev.ridill.rivo.settings.domain.repositoty

import dev.ridill.rivo.core.domain.util.DateUtil
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.util.Currency

interface CurrencyPreferenceRepository {
    fun getCurrencyPreferenceForDateOrNext(
        date: LocalDate = DateUtil.dateNow()
    ): Flow<Currency>

    suspend fun saveCurrency(
        currency: Currency,
        date: LocalDate = DateUtil.dateNow()
    )
}