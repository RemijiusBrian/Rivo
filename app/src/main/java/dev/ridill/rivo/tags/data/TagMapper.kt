package dev.ridill.rivo.tags.data

import androidx.compose.ui.graphics.Color
import dev.ridill.rivo.tags.data.local.entity.TagEntity
import dev.ridill.rivo.tags.domain.model.Tag
import dev.ridill.rivo.tags.domain.model.TagInfo
import dev.ridill.rivo.transactions.data.local.relation.TagAndAggregateRelation

fun TagEntity.toTag(): Tag = Tag(
    id = id,
    name = name,
    colorCode = colorCode,
    createdTimestamp = createdTimestamp,
    excluded = isExcluded
)

fun TagAndAggregateRelation.toTagInfo(): TagInfo = TagInfo(
    id = id,
    name = name,
    color = Color(colorCode),
    createdTimestamp = createdTimestamp,
    excluded = excluded,
    aggregate = aggregate
)

fun TagAndAggregateRelation.toTag(): Tag = Tag(
    id = id,
    name = name,
    colorCode = colorCode,
    createdTimestamp = createdTimestamp,
    excluded = excluded
)