package dev.ridill.rivo.tags.domain.model

import androidx.compose.ui.graphics.Color
import java.time.LocalDateTime

data class TagInfo(
    val id: Long,
    val name: String,
    val color: Color,
    val createdTimestamp: LocalDateTime,
    val excluded: Boolean,
    val expenditure: Double
)