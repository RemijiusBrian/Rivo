package dev.ridill.rivo.transactions.data

import dev.ridill.rivo.core.ui.util.TextFormat
import dev.ridill.rivo.transactions.data.local.entity.TransactionEntity
import dev.ridill.rivo.transactions.data.local.relations.TransactionWithTagRelation
import dev.ridill.rivo.transactions.domain.model.Expense
import dev.ridill.rivo.transactions.domain.model.ExpenseListItem
import dev.ridill.rivo.transactions.domain.model.ExpenseTag

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