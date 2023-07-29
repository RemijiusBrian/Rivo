package dev.ridill.mym.expense.domain.repository

import dev.ridill.mym.expense.domain.model.Expense

interface ExpenseRepository {

    suspend fun getExpenseById(id: Long): Expense?

    suspend fun cacheExpense(expense: Expense)
}