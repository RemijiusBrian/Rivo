package dev.ridill.rivo.settings.domain.repositoty

import dev.ridill.rivo.core.domain.util.DateUtil
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface BudgetRepository {
    suspend fun saveBudget(
        amount: Long,
        date: LocalDate = DateUtil.dateNow()
    )

    fun getBudgetAmountForDateOrLatest(
        date: LocalDate = DateUtil.dateNow()
    ): Flow<Long>
}