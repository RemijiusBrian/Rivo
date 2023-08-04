package dev.ridill.mym.expense.data

import androidx.compose.ui.graphics.Color
import dev.ridill.mym.core.domain.util.Zero
import dev.ridill.mym.expense.data.local.entity.TagEntity
import dev.ridill.mym.expense.data.local.relations.TagWithExpenditureRelation
import dev.ridill.mym.expense.domain.model.ExpenseTag
import dev.ridill.mym.expense.domain.model.TagWithExpenditure

fun TagEntity.toExpenseTag(): ExpenseTag = ExpenseTag(
    name = name,
    color = Color(colorCode),
    createdTimestamp = dateCreated
)

fun TagWithExpenditureRelation.toTagWithExpenditure(
    totalExpenditure: Double
): TagWithExpenditure = TagWithExpenditure(
    tag = ExpenseTag(
        name = name,
        color = Color(colorCode),
        createdTimestamp = createdTimestamp
    ),
    expenditure = amount,
    percentOfTotalExpenditure = Float.Zero
//    ((expenditure ?: Double.Zero) / totalExpenditure).toFloat()
)