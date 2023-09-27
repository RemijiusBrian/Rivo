package dev.ridill.rivo.transactions.data

import dev.ridill.rivo.transactionGroups.domain.model.TxGroup
import dev.ridill.rivo.transactions.data.local.entity.TransactionEntity
import dev.ridill.rivo.transactions.data.local.relations.TransactionDetails
import dev.ridill.rivo.transactions.domain.model.Transaction
import dev.ridill.rivo.transactions.domain.model.TransactionType
import dev.ridill.rivo.transactions.domain.model.TransactionListItem
import dev.ridill.rivo.transactions.domain.model.TransactionTag

fun TransactionEntity.toTransaction(): Transaction = Transaction(
    id = id,
    amount = amount.toString(),
    note = note,
    createdTimestamp = timestamp,
    type = TransactionType.valueOf(typeName),
    groupId = groupId,
    tagId = tagId,
    excluded = isExcluded
)

fun TransactionDetails.toTransactionListItem(): TransactionListItem {
    val tag = if (tagId != null
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
    else null

    val transactionGroup = if (groupId != null
        && groupName != null
        && groupCreatedTimestamp != null
    ) TxGroup(
        id = groupId,
        name = groupName,
        createdTimestamp = groupCreatedTimestamp,
        excluded = isExcludedTransaction
    ) else null

    return TransactionListItem(
        id = transactionId,
        note = transactionNote,
        amount = transactionAmount,
        date = transactionTimestamp.toLocalDate(),
        type = TransactionType.valueOf(transactionTypeName),
        tag = tag,
        group = transactionGroup,
        excluded = isExcludedTransaction
    )
}