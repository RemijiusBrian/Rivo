package dev.ridill.mym.expense.domain.model

data class TagWithExpenditure(
    val tag: ExpenseTag,
    val expenditure: Double,
    val percentOfTotalExpenditure: Float
)