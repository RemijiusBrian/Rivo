package dev.ridill.rivo.transactions.domain.model

import java.time.LocalDate

sealed class TransactionListItemUiModel {
    data class TransactionItem(val transaction: TransactionListItem) : TransactionListItemUiModel()
    data class DateSeparator(val date: LocalDate) : TransactionListItemUiModel()
}