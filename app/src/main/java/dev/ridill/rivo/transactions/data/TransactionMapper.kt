package dev.ridill.rivo.transactions.data

import dev.ridill.rivo.folders.domain.model.Folder
import dev.ridill.rivo.transactions.data.local.entity.TransactionEntity
import dev.ridill.rivo.transactions.data.local.views.TransactionDetailsView
import dev.ridill.rivo.transactions.domain.model.Tag
import dev.ridill.rivo.transactions.domain.model.Transaction
import dev.ridill.rivo.transactions.domain.model.TransactionListItem
import dev.ridill.rivo.transactions.domain.model.TransactionType

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

fun TransactionDetailsView.toTransactionListItem(): TransactionListItem {
    val tag = if (tagId != null
        && tagName != null
        && tagColorCode != null
        && tagCreatedTimestamp != null
    ) Tag(
        id = tagId,
        name = tagName,
        colorCode = tagColorCode,
        createdTimestamp = tagCreatedTimestamp,
        excluded = isTagExcluded == true
    )
    else null

    val folder = if (folderId != null
        && folderName != null
        && folderCreatedTimestamp != null
    ) Folder(
        id = folderId,
        name = folderName,
        createdTimestamp = folderCreatedTimestamp,
        excluded = isFolderExcluded == true
    ) else null

    return TransactionListItem(
        id = transactionId,
        note = transactionNote,
        amount = transactionAmount,
        timestamp = transactionTimestamp,
        type = TransactionType.valueOf(transactionTypeName),
        isTransactionExcluded = isTransactionExcluded,
        tag = tag,
        folder = folder
    )
}

fun TransactionListItem.toEntity(): TransactionEntity = TransactionEntity(
    id = id,
    note = note,
    amount = amount,
    timestamp = timestamp,
    typeName = type.name,
    isExcluded = isTransactionExcluded,
    tagId = tag?.id,
    folderId = folder?.id
)