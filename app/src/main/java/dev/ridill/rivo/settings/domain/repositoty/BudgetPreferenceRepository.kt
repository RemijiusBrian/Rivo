package dev.ridill.rivo.settings.domain.repositoty

import dev.ridill.rivo.core.domain.util.DateUtil
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface BudgetPreferenceRepository {
    fun getBudgetPreferenceForDateOrNext(
        date: LocalDate = DateUtil.dateNow()
    ): Flow<Long>

    suspend fun saveBudgetPreference(
        amount: Long,
        date: LocalDate = DateUtil.dateNow()
    )
}