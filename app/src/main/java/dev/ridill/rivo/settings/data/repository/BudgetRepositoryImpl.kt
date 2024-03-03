package dev.ridill.rivo.settings.data.repository

import dev.ridill.rivo.settings.data.local.BudgetDao
import dev.ridill.rivo.settings.data.local.entity.BudgetEntity
import dev.ridill.rivo.settings.domain.repositoty.BudgetRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.time.LocalDate

class BudgetRepositoryImpl(
    private val dao: BudgetDao
) : BudgetRepository {
    override suspend fun saveBudget(amount: Long, date: LocalDate) {
        withContext(Dispatchers.IO) {
            val entity = BudgetEntity(
                amount = amount,
                date = date.withDayOfMonth(1) // Set day to 1 on all dates to limit date comparison to year and month
            )
            dao.insert(entity)
        }
    }

    override fun getBudgetAmountForDateOrNext(date: LocalDate): Flow<Long> =
        dao.getBudgetAmountForDateOrNext(date)
}