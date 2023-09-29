package dev.ridill.rivo.transactions.domain.model

import java.time.LocalDate

sealed class TransactionListItemUIModel {
    data class TransactionItem(val transaction: TransactionListItem) : TransactionListItemUIModel()
    data class DateSeparator(val date: LocalDate) : TransactionListItemUIModel()
}