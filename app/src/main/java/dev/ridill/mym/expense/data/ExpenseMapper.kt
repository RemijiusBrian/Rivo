package dev.ridill.mym.expense.data

import dev.ridill.mym.core.ui.util.TextFormat
import dev.ridill.mym.expense.data.local.entity.ExpenseEntity
import dev.ridill.mym.expense.data.local.relations.ExpenseWithTagRelation
import dev.ridill.mym.expense.domain.model.Expense
import dev.ridill.mym.expense.domain.model.ExpenseListItem
import dev.ridill.mym.expense.domain.model.ExpenseTag

fun ExpenseEntity.toExpense(): Expense = Expense(
    id = id,
    amount = amount.toString(),
    note = note,
    createdTimestamp = timestamp,
    tagId = tagId,
    excluded = isExcluded
)

fun ExpenseWithTagRelation.toExpenseListItem(): ExpenseListItem = ExpenseListItem(
    id = expenseId,
    note = expenseNote,
    amount = TextFormat.currency(expenseAmount),
    date = expenseTimestamp.toLocalDate(),
    tag = if (
        tagId != null
        && tagName != null
        && tagColorCode != null
        && tagCreatedTimestamp != null
    ) ExpenseTag(
        id = tagId,
        name = tagName,
        colorCode = tagColorCode,
        createdTimestamp = tagCreatedTimestamp,
        excluded = isExcludedTransaction
    )
    else null,
    excluded = isExcludedTransaction
)