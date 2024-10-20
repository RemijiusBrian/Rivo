package dev.ridill.rivo.transactions.domain.model

import java.time.LocalDate

data class TransactionDateLimits(
    val maxDate: LocalDate,
    val minDate: LocalDate
)