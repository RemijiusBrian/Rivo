package dev.ridill.rivo.transactions.data

import dev.ridill.rivo.transactionFolders.domain.model.TransactionFolder
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
    timestamp = timestamp,
    type = TransactionType.valueOf(typeName),
    folderId = folderId,
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

    val transactionFolder = if (folderId != null
        && folderName != null
        && folderCreatedTimestamp != null
    ) TransactionFolder(
        id = folderId,
        name = folderName,
        createdTimestamp = folderCreatedTimestamp,
        excluded = isExcludedTransaction
    ) else null

    return TransactionListItem(
        id = transactionId,
        note = transactionNote,
        amount = transactionAmount,
        date = transactionTimestamp.toLocalDate(),
        type = TransactionType.valueOf(transactionTypeName),
        tag = tag,
        folder = transactionFolder,
        excluded = isExcludedTransaction
    )
}