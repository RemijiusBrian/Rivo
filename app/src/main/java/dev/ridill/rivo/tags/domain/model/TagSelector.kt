package dev.ridill.rivo.tags.domain.model

import androidx.compose.ui.graphics.Color

data class TagSelector(
    val id: Long,
    val name: String,
    val color: Color,
    val excluded: Boolean
)