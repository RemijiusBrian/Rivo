package dev.ridill.mym.expense.data

import dev.ridill.mym.expense.data.local.entity.TagEntity
import dev.ridill.mym.expense.data.local.relations.TagWithExpenditureRelation
import dev.ridill.mym.expense.domain.model.ExpenseTag
import dev.ridill.mym.expense.domain.model.TagWithExpenditure

fun TagEntity.toExpenseTag(): ExpenseTag = ExpenseTag(
    id = id,
    name = name,
    colorCode = colorCode,
    createdTimestamp = createdTimestamp
)

fun TagWithExpenditureRelation.toTagWithExpenditure(
    totalExpenditure: Double
): TagWithExpenditure = TagWithExpenditure(
    tag = ExpenseTag(
        id = id,
        name = name,
        colorCode = colorCode,
        createdTimestamp = createdTimestamp
    ),
    expenditure = amount,
    percentOfTotalExpenditure = (amount / totalExpenditure).toFloat()
)