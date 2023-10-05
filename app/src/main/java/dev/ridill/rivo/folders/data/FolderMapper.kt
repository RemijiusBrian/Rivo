package dev.ridill.rivo.folders.data

import dev.ridill.rivo.folders.data.local.entity.FolderEntity
import dev.ridill.rivo.folders.data.local.relation.FolderAndAggregateAmount
import dev.ridill.rivo.folders.domain.model.TransactionFolder
import dev.ridill.rivo.folders.domain.model.FolderDetails

fun FolderAndAggregateAmount.toTransactionFolderDetails(): FolderDetails = FolderDetails(
    id = id,
    name = name,
    createdTimestamp = createdTimestamp,
    excluded = excluded,
    aggregateAmount = aggregateAmount
)

fun FolderEntity.toTransactionFolder(): TransactionFolder = TransactionFolder(
    id = id,
    name = name,
    createdTimestamp = createdTimestamp,
    excluded = isExcluded
)