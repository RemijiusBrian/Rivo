package dev.ridill.mym.expense.data

import dev.ridill.mym.core.ui.util.TextFormat
import dev.ridill.mym.expense.data.local.entity.TransactionEntity
import dev.ridill.mym.expense.data.local.relations.TransactionWithTagRelation
import dev.ridill.mym.expense.domain.model.Expense
import dev.ridill.mym.expense.domain.model.ExpenseListItem
import dev.ridill.mym.expense.domain.model.ExpenseTag

fun TransactionEntity.toExpense(): Expense = Expense(
    id = id,
    amount = amount.toString(),
    note = note,
    createdTimestamp = timestamp,
    tagId = tagId,
    excluded = isExcluded
)

fun TransactionWithTagRelation.toExpenseListItem(): ExpenseListItem = ExpenseListItem(
    id = transactionId,
    note = transactionNote,
    amount = TextFormat.currency(transactionAmount),
    date = transactionTimestamp.toLocalDate(),
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