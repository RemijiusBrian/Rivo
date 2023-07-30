package dev.ridill.mym.expense.domain.model

import androidx.compose.ui.graphics.Color
import java.time.LocalDateTime

data class ExpenseTag(
    val name: String,
    val color: Color,
    val createdTimestamp: LocalDateTime
)