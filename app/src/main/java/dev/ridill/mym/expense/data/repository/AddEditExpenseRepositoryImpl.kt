package dev.ridill.mym.expense.data.repository

import dev.ridill.mym.core.domain.util.Zero
import dev.ridill.mym.expense.data.local.ExpenseDao
import dev.ridill.mym.expense.data.local.TagsDao
import dev.ridill.mym.expense.data.local.entity.ExpenseEntity
import dev.ridill.mym.expense.data.local.entity.TagEntity
import dev.ridill.mym.expense.data.toExpense
import dev.ridill.mym.expense.data.toExpenseTag
import dev.ridill.mym.expense.domain.model.Expense
import dev.ridill.mym.expense.domain.model.ExpenseTag
import dev.ridill.mym.expense.domain.repository.AddEditExpenseRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import kotlin.math.roundToLong

class AddEditExpenseRepositoryImpl(
    private val expenseDao: ExpenseDao,
    private val tagsDao: TagsDao
) : AddEditExpenseRepository {
    override suspend fun getExpenseById(id: Long): Expense? = withContext(Dispatchers.IO) {
        expenseDao.getExpenseById(id)?.toExpense()
    }

    override fun getAmountRecommendations(): Flow<List<Long>> = expenseDao.getExpenseRange()
        .map { (upperLimit, lowerLimit) ->
            val roundUpper = (upperLimit.roundToLong() / 10) * 10
            val roundLower = (lowerLimit.roundToLong() / 10) * 10

            val range = roundUpper - roundLower

            if (range == Long.Zero) listOf(50L, 100L, 500L)
            else listOf(roundLower, roundLower + (range / 2), roundUpper)
        }

    override fun getTagsList(): Flow<List<ExpenseTag>> =
        tagsDao.getTagsList().map { entities ->
            entities.map(TagEntity::toExpenseTag)
        }

    override suspend fun cacheExpense(
        id: Long,
        amount: Double,
        note: String,
        dateTime: LocalDateTime,
        tagId: String?
    ) = withContext(Dispatchers.IO) {
        val entity = ExpenseEntity(
            id = id,
            note = note,
            amount = amount,
            dateTime = dateTime,
            tagId = tagId
        )
        expenseDao.insert(entity)
    }

    override suspend fun deleteExpense(id: Long) = withContext(Dispatchers.IO) {
        expenseDao.deleteExpenseById(id)
    }
}