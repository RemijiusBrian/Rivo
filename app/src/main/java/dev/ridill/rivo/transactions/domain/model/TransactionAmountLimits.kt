package dev.ridill.rivo.transactions.domain.model

data class TransactionAmountLimits(
    val upperLimit: Double,
    val lowerLimit: Double
)