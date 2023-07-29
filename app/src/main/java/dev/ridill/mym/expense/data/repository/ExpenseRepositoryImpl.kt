package dev.ridill.mym.expense.data.repository

import dev.ridill.mym.expense.data.local.ExpenseDao
import dev.ridill.mym.expense.data.toEntity
import dev.ridill.mym.expense.data.toExpense
import dev.ridill.mym.expense.domain.model.Expense
import dev.ridill.mym.expense.domain.repository.ExpenseRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ExpenseRepositoryImpl(
    private val dao: ExpenseDao
) : ExpenseRepository {
    override suspend fun getExpenseById(id: Long): Expense? = withContext(Dispatchers.IO) {
        dao.getExpenseById(id)?.toExpense()
    }

    override suspend fun cacheExpense(expense: Expense) = withContext(Dispatchers.IO) {
        dao.insert(expense.toEntity())
    }
}