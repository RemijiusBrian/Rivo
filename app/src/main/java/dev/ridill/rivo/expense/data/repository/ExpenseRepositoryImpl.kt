package dev.ridill.rivo.expense.data.repository

import dev.ridill.rivo.core.data.preferences.PreferencesManager
import dev.ridill.rivo.core.domain.util.DateUtil
import dev.ridill.rivo.core.domain.util.Zero
import dev.ridill.rivo.expense.data.local.TransactionDao
import dev.ridill.rivo.expense.data.local.entity.TransactionEntity
import dev.ridill.rivo.expense.data.local.relations.TransactionWithTagRelation
import dev.ridill.rivo.expense.data.toExpense
import dev.ridill.rivo.expense.data.toExpenseListItem
import dev.ridill.rivo.expense.domain.model.Expense
import dev.ridill.rivo.expense.domain.model.ExpenseListItem
import dev.ridill.rivo.expense.domain.repository.ExpenseRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.math.roundToLong

class ExpenseRepositoryImpl(
    private val dao: TransactionDao,
    private val preferencesManager: PreferencesManager
) : ExpenseRepository {
    override suspend fun getExpenseById(id: Long): Expense? = withContext(Dispatchers.IO) {
        dao.getTransactionById(id)?.toExpense()
    }

    override fun getAmountRecommendations(): Flow<List<Long>> = dao.getTransactionAmountRange()
        .map { (upperLimit, lowerLimit) ->
            val roundUpper = (upperLimit.roundToLong() / 10) * 10
            val roundLower = (lowerLimit.roundToLong() / 10) * 10

            val range = roundUpper - roundLower

            if (range == Long.Zero) listOf(50L, 100L, 500L)
            else listOf(roundLower, roundLower + (range / 2), roundUpper)
        }

    override suspend fun cacheExpense(
        id: Long?,
        amount: Double,
        note: String,
        dateTime: LocalDateTime,
        tagId: Long?,
        excluded: Boolean,
        groupId: Long?
    ): Long = withContext(Dispatchers.IO) {
        val entity = TransactionEntity(
            id = id ?: Long.Zero,
            note = note,
            amount = amount,
            timestamp = dateTime,
            tagId = tagId,
            isExcluded = excluded,
            groupId = groupId
        )
        dao.insert(entity).first()
    }

    override suspend fun deleteExpense(id: Long) = withContext(Dispatchers.IO) {
        dao.deleteTransactionById(id)
    }

    override suspend fun deleteExpenses(ids: List<Long>) = withContext(Dispatchers.IO) {
        dao.deleteMultipleTransactionsById(ids)
    }

    override fun getExpenseYearsList(paddingCount: Int): Flow<List<Int>> =
        dao.getYearsFromTransactions()
            .map { years ->
                if (years.size >= paddingCount) years
                else {
                    val difference = paddingCount - years.size
                    val latestYear = years.lastOrNull() ?: (DateUtil.now().year - 1)
                    val paddingYears = ((latestYear + 1)..(latestYear + difference))
                    years + paddingYears
                }
            }

    override fun getTotalExpenditureForDate(date: LocalDate): Flow<Double> =
        dao.getExpenditureForMonth(date.format(DateUtil.Formatters.MM_yyyy_dbFormat))

    override fun getExpenseForDateByTag(
        date: LocalDate,
        tagId: Long?,
        showExcluded: Boolean
    ): Flow<List<ExpenseListItem>> = dao.getTransactionForMonthByTag(
        monthAndYear = date.format(DateUtil.Formatters.MM_yyyy_dbFormat),
        tagId = tagId,
        showExcluded = showExcluded
    ).map { entities -> entities.map(TransactionWithTagRelation::toExpenseListItem) }

    override fun getShowExcludedExpenses(): Flow<Boolean> =
        preferencesManager.preferences.map { it.showExcludedExpenses }
            .distinctUntilChanged()

    override suspend fun toggleShowExcludedExpenses(show: Boolean) =
        preferencesManager.updateShowExcludedExpenses(show)

    override suspend fun toggleExpenseExclusionByIds(ids: List<Long>, excluded: Boolean) =
        withContext(Dispatchers.IO) {
            dao.toggleExclusionByIds(ids, excluded)
        }
}