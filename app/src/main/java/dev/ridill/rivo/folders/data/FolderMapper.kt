package dev.ridill.rivo.folders.data

import dev.ridill.rivo.folders.data.local.entity.FolderEntity
import dev.ridill.rivo.folders.data.local.relation.FolderAndAggregateAmount
import dev.ridill.rivo.folders.domain.model.Folder
import dev.ridill.rivo.folders.domain.model.FolderDetails

fun FolderAndAggregateAmount.toFolderDetails(): FolderDetails = FolderDetails(
    id = id,
    name = name,
    createdTimestamp = createdTimestamp,
    excluded = excluded,
    aggregateAmount = aggregateAmount
)

fun FolderEntity.toFolder(): Folder = Folder(
    id = id,
    name = name,
    createdTimestamp = createdTimestamp,
    excluded = isExcluded
)