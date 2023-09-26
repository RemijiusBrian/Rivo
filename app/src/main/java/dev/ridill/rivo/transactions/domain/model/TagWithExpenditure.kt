package dev.ridill.rivo.transactions.domain.model

data class TagWithExpenditure(
    val tag: TransactionTag,
    val expenditure: Double,
    val percentOfTotalExpenditure: Float
)