package dev.ridill.rivo.expense.domain.model

data class TransactionAmountLimits(
    val upperLimit: Double,
    val lowerLimit: Double
)