package dev.ridill.rivo.transactions.presentation.allTransactions

import dev.ridill.rivo.transactions.domain.model.TransactionTypeFilter
import java.time.Month

interface AllTransactionsActions {
    fun onMonthSelect(month: Month)
    fun onYearSelect(year: Int)
    fun onTypeFilterSelect(filter: TransactionTypeFilter)
    fun onShowExcludedToggle(showExcluded: Boolean)
    fun onTransactionLongPress(id: Long)
    fun onTransactionSelectionChange(id: Long)
    fun onSelectionStateChange()
    fun onDismissMultiSelectionMode()
    fun onMultiSelectionOptionsClick()
    fun onMultiSelectionOptionsDismiss()
    fun onMultiSelectionOptionSelect(option: AllTransactionsMultiSelectionOption)
    fun onDeleteTransactionDismiss()
    fun onDeleteTransactionConfirm()
    fun onAggregationDismiss()
    fun onAggregationConfirm()
    fun onFilterOptionsClick()
    fun onFilterOptionsDismiss()
}