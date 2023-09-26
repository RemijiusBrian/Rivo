package dev.ridill.rivo.transactions.presentation.allTransactions

import androidx.compose.ui.graphics.Color
import dev.ridill.rivo.transactions.domain.model.TransactionOption
import dev.ridill.rivo.transactions.domain.model.TransactionTag
import java.time.Month

interface AllTransactionsActions {
    fun onMonthSelect(month: Month)
    fun onYearSelect(year: Int)
    fun onTagClick(tag: TransactionTag)
    fun onNewTagClick()
    fun onTagInputNameChange(value: String)
    fun onTagInputColorSelect(color: Color)
    fun onTagInputExclusionChange(excluded: Boolean)
    fun onTagInputDismiss()
    fun onTagInputConfirm()
    fun onToggleShowExcludedTransactions(value: Boolean)
    fun onTransactionLongClick(id: Long)
    fun onTransactionClick(id: Long)
    fun onSelectionStateChange()
    fun onDismissMultiSelectionMode()
    fun onTransactionOptionClick(option: TransactionOption)
    fun onDeleteSelectedTransactionsClick()
    fun onDeleteTransactionDismiss()
    fun onDeleteTransactionConfirm()
    fun onEditTagClick(tag: TransactionTag)
    fun onDeleteTagClick()
    fun onDeleteTagDismiss()
    fun onDeleteTagConfirm()
    fun onDeleteTagWithTransactionsClick()
}