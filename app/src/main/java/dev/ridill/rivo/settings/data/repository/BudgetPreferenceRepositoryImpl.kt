package dev.ridill.rivo.settings.data.repository

import dev.ridill.rivo.settings.data.local.BudgetPreferenceDao
import dev.ridill.rivo.settings.data.local.entity.BudgetPreferenceEntity
import dev.ridill.rivo.settings.domain.repositoty.BudgetPreferenceRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.time.LocalDate

class BudgetPreferenceRepositoryImpl(
    private val dao: BudgetPreferenceDao
) : BudgetPreferenceRepository {
    override fun getBudgetPreferenceForMonth(date: LocalDate): Flow<Long> =
        dao.getAmountForDateOrLast(date)

    override suspend fun saveBudgetPreference(amount: Long, date: LocalDate) {
        withContext(Dispatchers.IO) {
            val entity = BudgetPreferenceEntity(
                amount = amount,
                date = date.withDayOfMonth(1) // Set day to 1 on all dates to limit date comparison to year and month
            )
            dao.upsert(entity)
        }
    }
}