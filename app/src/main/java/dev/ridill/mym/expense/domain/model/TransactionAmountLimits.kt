package dev.ridill.mym.expense.domain.model

data class TransactionAmountLimits(
    val upperLimit: Double,
    val lowerLimit: Double
)