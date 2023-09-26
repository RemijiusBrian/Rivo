package dev.ridill.rivo.expense.data

import dev.ridill.rivo.expense.data.local.entity.TagEntity
import dev.ridill.rivo.expense.data.local.relations.TagWithExpenditureRelation
import dev.ridill.rivo.expense.domain.model.ExpenseTag
import dev.ridill.rivo.expense.domain.model.TagWithExpenditure

fun TagEntity.toExpenseTag(): ExpenseTag = ExpenseTag(
    id = id,
    name = name,
    colorCode = colorCode,
    createdTimestamp = createdTimestamp,
    excluded = isExcluded
)

fun TagWithExpenditureRelation.toTagWithExpenditure(
    totalExpenditure: Double
): TagWithExpenditure = TagWithExpenditure(
    tag = ExpenseTag(
        id = id,
        name = name,
        colorCode = colorCode,
        createdTimestamp = createdTimestamp,
        excluded = isExcluded
    ),
    expenditure = amount,
    percentOfTotalExpenditure = (amount / totalExpenditure).toFloat()
)