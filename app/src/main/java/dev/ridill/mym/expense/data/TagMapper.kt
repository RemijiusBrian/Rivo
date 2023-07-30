package dev.ridill.mym.expense.data

import androidx.compose.ui.graphics.Color
import dev.ridill.mym.expense.data.local.entity.TagEntity
import dev.ridill.mym.expense.domain.model.ExpenseTag

fun TagEntity.toExpenseTag(): ExpenseTag = ExpenseTag(
    name = name,
    color = Color(colorCode),
    createdTimestamp = dateCreated
)