package dev.ridill.rivo.folders.data

import dev.ridill.rivo.folders.data.local.entity.FolderEntity
import dev.ridill.rivo.folders.data.local.views.FolderAndAggregateAmountView
import dev.ridill.rivo.folders.domain.model.Folder
import dev.ridill.rivo.folders.domain.model.FolderDetails

fun FolderAndAggregateAmountView.toFolderDetails(): FolderDetails = FolderDetails(
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