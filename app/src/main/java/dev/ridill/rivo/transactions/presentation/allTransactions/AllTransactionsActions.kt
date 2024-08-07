package dev.ridill.rivo.transactions.presentation.allTransactions

import java.time.Month

interface AllTransactionsActions {
    fun onMonthSelect(month: Month)
    fun onYearSelect(year: Int)
    fun onTagSelect(tagId: Long)
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