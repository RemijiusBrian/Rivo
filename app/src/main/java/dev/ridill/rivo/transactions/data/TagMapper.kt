package dev.ridill.rivo.transactions.data

import dev.ridill.rivo.transactions.data.local.entity.TagEntity
import dev.ridill.rivo.transactions.data.local.relations.TagWithExpenditureRelation
import dev.ridill.rivo.transactions.domain.model.ExpenseTag
import dev.ridill.rivo.transactions.domain.model.TagWithExpenditure

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