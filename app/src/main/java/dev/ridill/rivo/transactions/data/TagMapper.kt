package dev.ridill.rivo.transactions.data

import androidx.compose.ui.graphics.Color
import dev.ridill.rivo.transactions.data.local.entity.TagEntity
import dev.ridill.rivo.transactions.domain.model.Tag
import dev.ridill.rivo.transactions.domain.model.TagSelector

fun TagEntity.toTagSelector(): TagSelector = TagSelector(
    id = id,
    name = name,
    color = Color(colorCode),
    excluded = isExcluded
)

fun TagEntity.toTag(): Tag = Tag(
    id = id,
    name = name,
    colorCode = colorCode,
    createdTimestamp = createdTimestamp,
    excluded = isExcluded
)