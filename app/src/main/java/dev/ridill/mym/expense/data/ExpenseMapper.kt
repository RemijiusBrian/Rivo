package dev.ridill.mym.expense.data

import dev.ridill.mym.core.domain.util.TextFormatUtil
import dev.ridill.mym.expense.data.local.entity.ExpenseEntity
import dev.ridill.mym.expense.data.local.relations.ExpenseWithTagRelation
import dev.ridill.mym.expense.domain.model.Expense
import dev.ridill.mym.expense.domain.model.ExpenseListItem

fun ExpenseEntity.toExpense(): Expense = Expense(
    id = id,
    amount = amount.toString(),
    note = note,
    createdTimestamp = dateTime,
    tagId = tagId
)

fun ExpenseWithTagRelation.toRecentSpend(): ExpenseListItem = ExpenseListItem(
    id = expenseEntity.id,
    note = expenseEntity.note,
    amount = TextFormatUtil.currency(expenseEntity.amount),
    date = expenseEntity.dateTime.toLocalDate(),
    tag = tagEntity?.toExpenseTag()
)