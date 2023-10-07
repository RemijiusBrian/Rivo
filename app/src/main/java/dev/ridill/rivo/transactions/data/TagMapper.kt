package dev.ridill.rivo.transactions.data

import androidx.compose.ui.graphics.Color
import dev.ridill.rivo.transactions.data.local.entity.TagEntity
import dev.ridill.rivo.transactions.data.local.relations.TagWithExpenditureRelation
import dev.ridill.rivo.transactions.domain.model.Tag
import dev.ridill.rivo.transactions.domain.model.TagInfo

fun TagEntity.toTag(): Tag = Tag(
    id = id,
    name = name,
    colorCode = colorCode,
    createdTimestamp = createdTimestamp,
    excluded = isExcluded
)

fun TagWithExpenditureRelation.toTagInfo(): TagInfo = TagInfo(
    id = id,
    name = name,
    color = Color(colorCode),
    createdTimestamp = createdTimestamp,
    excluded = isExcluded,
    expenditure = amount
)