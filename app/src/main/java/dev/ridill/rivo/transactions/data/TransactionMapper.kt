package dev.ridill.rivo.transactions.data

import dev.ridill.rivo.core.ui.util.TextFormat
import dev.ridill.rivo.transactions.data.local.entity.TransactionEntity
import dev.ridill.rivo.transactions.data.local.relations.TransactionDetails
import dev.ridill.rivo.transactions.domain.model.Transaction
import dev.ridill.rivo.transactions.domain.model.TransactionListItem
import dev.ridill.rivo.transactions.domain.model.TransactionTag

fun TransactionEntity.toTransaction(): Transaction = Transaction(
    id = id,
    amount = amount.toString(),
    note = note,
    createdTimestamp = timestamp,
    tagId = tagId,
    excluded = isExcluded
)

fun TransactionDetails.toTransactionListItem(): TransactionListItem = TransactionListItem(
    id = transactionId,
    note = transactionNote,
    amount = TextFormat.currency(transactionAmount),
    date = transactionTimestamp.toLocalDate(),
    tag = if (
        tagId != null
        && tagName != null
        && tagColorCode != null
        && tagCreatedTimestamp != null
    ) TransactionTag(
        id = tagId,
        name = tagName,
        colorCode = tagColorCode,
        createdTimestamp = tagCreatedTimestamp,
        excluded = isExcludedTransaction
    )
    else null,
    excluded = isExcludedTransaction
)