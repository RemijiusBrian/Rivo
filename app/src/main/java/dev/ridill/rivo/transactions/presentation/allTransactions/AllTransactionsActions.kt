package dev.ridill.rivo.transactions.presentation.allTransactions

import androidx.compose.ui.graphics.Color
import dev.ridill.rivo.folders.domain.model.Folder
import dev.ridill.rivo.transactions.domain.model.TransactionOption
import java.time.Month

interface AllTransactionsActions {
    fun onMonthSelect(month: Month)
    fun onYearSelect(year: Int)
    fun onTagClick(tagId: Long)
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
    fun onTransactionFolderQueryChange(query: String)
    fun onTransactionFolderSelectionDismiss()
    fun onTransactionFolderSelect(folder: Folder)
    fun onCreateNewFolderClick()
    fun onDeleteSelectedTransactionsClick()
    fun onDeleteTransactionDismiss()
    fun onDeleteTransactionConfirm()
    fun onTagLongClick(tagId: Long)
    fun onDeleteTagClick()
    fun onDeleteTagDismiss()
    fun onDeleteTagConfirm()
    fun onDeleteTagWithTransactionsClick()
}