package dev.ridill.rivo.transactions.domain.model

data class TagWithExpenditure(
    val tag: ExpenseTag,
    val expenditure: Double,
    val percentOfTotalExpenditure: Float
)