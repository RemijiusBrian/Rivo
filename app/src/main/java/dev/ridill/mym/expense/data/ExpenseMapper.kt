package dev.ridill.mym.expense.data

import dev.ridill.mym.core.domain.util.Formatter
import dev.ridill.mym.dashboard.domain.model.RecentSpend
import dev.ridill.mym.expense.data.local.entity.ExpenseEntity
import dev.ridill.mym.expense.data.local.relations.ExpenseWithTag
import dev.ridill.mym.expense.domain.model.Expense

fun ExpenseEntity.toExpense(): Expense = Expense(
    id = id,
    amount = amount.toString(),
    note = note,
    dateTime = dateTime
)

fun ExpenseWithTag.toRecentSpend(): RecentSpend = RecentSpend(
    id = expenseEntity.id,
    note = expenseEntity.note,
    amount = Formatter.currency(expenseEntity.amount),
    dateTime = expenseEntity.dateTime,
    tag = tagEntity?.toExpenseTag()
)