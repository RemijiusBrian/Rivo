package dev.ridill.rivo.transactions.data

import dev.ridill.rivo.transactions.data.local.entity.TagEntity
import dev.ridill.rivo.transactions.data.local.relations.TagWithExpenditureRelation
import dev.ridill.rivo.transactions.domain.model.TransactionTag
import dev.ridill.rivo.transactions.domain.model.TagWithExpenditure

fun TagEntity.toTransactionTag(): TransactionTag = TransactionTag(
    id = id,
    name = name,
    colorCode = colorCode,
    createdTimestamp = createdTimestamp,
    excluded = isExcluded
)

fun TagWithExpenditureRelation.toTagWithExpenditure(
    totalExpenditure: Double
): TagWithExpenditure = TagWithExpenditure(
    tag = TransactionTag(
        id = id,
        name = name,
        colorCode = colorCode,
        createdTimestamp = createdTimestamp,
        excluded = isExcluded
    ),
    expenditure = amount,
    percentOfTotalExpenditure = (amount / totalExpenditure).toFloat()
)