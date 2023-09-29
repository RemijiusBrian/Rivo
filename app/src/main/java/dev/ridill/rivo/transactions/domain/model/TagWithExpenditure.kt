package dev.ridill.rivo.transactions.domain.model

data class TagWithExpenditure(
    val tag: Tag,
    val expenditure: Double,
    val percentOfTotalExpenditure: Float
)