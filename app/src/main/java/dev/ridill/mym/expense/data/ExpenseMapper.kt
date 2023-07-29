package dev.ridill.mym.expense.data

import dev.ridill.mym.core.domain.util.DateUtil
import dev.ridill.mym.core.domain.util.Formatter
import dev.ridill.mym.core.domain.util.Zero
import dev.ridill.mym.dashboard.domain.model.RecentTransaction
import dev.ridill.mym.expense.data.local.entity.ExpenseEntity
import dev.ridill.mym.expense.domain.model.Expense

fun ExpenseEntity.toExpense(): Expense = Expense(
    id = id,
    amount = amount.toString(),
    note = note
)

fun Expense.toEntity(): ExpenseEntity = ExpenseEntity(
    id = id,
    note = note,
    amount = amount.toDoubleOrNull() ?: Double.Zero,
    dateTime = DateUtil.now()
)

fun ExpenseEntity.toRecentTransaction(): RecentTransaction = RecentTransaction(
    id = id,
    note = note,
    amount = Formatter.currency(amount),
    dateTime = dateTime
)