package dev.ridill.rivo.transactionFolders.data

import dev.ridill.rivo.transactionFolders.data.local.entity.TransactionFolderEntity
import dev.ridill.rivo.transactionFolders.data.local.relation.FolderAndAggregateAmount
import dev.ridill.rivo.transactionFolders.domain.model.TransactionFolder
import dev.ridill.rivo.transactionFolders.domain.model.TransactionFolderDetails

fun FolderAndAggregateAmount.toTransactionFolderDetails(): TransactionFolderDetails = TransactionFolderDetails(
    id = id,
    name = name,
    createdTimestamp = createdTimestamp,
    excluded = excluded,
    aggregateAmount = aggregateAmount
)

fun TransactionFolderEntity.toTransactionFolder(): TransactionFolder = TransactionFolder(
    id = id,
    name = name,
    createdTimestamp = createdTimestamp,
    excluded = isExcluded
)