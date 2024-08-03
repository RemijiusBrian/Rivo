package dev.ridill.rivo.transactions.presentation.allTransactions

import androidx.compose.ui.graphics.Color
import java.time.Month

interface AllTransactionsActions {
    fun onMonthSelect(month: Month)
    fun onYearSelect(year: Int)
    fun onTagSelect(tagId: Long)
    fun onNewTagClick()
    fun onAssignTagToTransactions(tagId: Long)
    fun onTagInputNameChange(value: String)
    fun onTagInputColorSelect(color: Color)
    fun onTagInputExclusionChange(excluded: Boolean)
    fun onTagInputDismiss()
    fun onTagInputConfirm()
    fun onTransactionLongPress(id: Long)
    fun onTransactionSelectionChange(id: Long)
    fun onSelectionStateChange()
    fun onDismissMultiSelectionMode()
    fun onMultiSelectionOptionsClick()
    fun onMultiSelectionOptionsDismiss()
    fun onMultiSelectionOptionSelect(option: AllTransactionsMultiSelectionOption)
    fun onDeleteTransactionDismiss()
    fun onDeleteTransactionConfirm()
    fun onTagLongClick(tagId: Long)
    fun onDeleteTagClick()
    fun onDeleteTagDismiss()
    fun onDeleteTagConfirm()
    fun onDeleteTagWithTransactionsClick()
    fun onAggregationDismiss()
    fun onAggregationConfirm()
    fun onFilterOptionsClick()
    fun onFilterOptionsDismiss()
}