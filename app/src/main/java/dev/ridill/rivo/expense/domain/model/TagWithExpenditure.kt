package dev.ridill.rivo.expense.domain.model

data class TagWithExpenditure(
    val tag: ExpenseTag,
    val expenditure: Double,
    val percentOfTotalExpenditure: Float
)