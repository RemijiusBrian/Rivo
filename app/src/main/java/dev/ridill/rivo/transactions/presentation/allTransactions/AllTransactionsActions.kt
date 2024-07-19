package dev.ridill.rivo.transactions.presentation.allTransactions

import androidx.compose.ui.graphics.Color
import dev.ridill.rivo.folders.domain.model.Folder
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
    fun onTransactionTypeFilterToggle()
    fun onToggleShowExcludedOption(value: Boolean)
    fun onTransactionLongPress(id: Long)
    fun onTransactionSelectionChange(id: Long)
    fun onSelectionStateChange()
    fun onDismissMultiSelectionMode()
    fun onTransactionOptionClick(option: AllTransactionsMultiSelectionOption)
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
    fun onAggregationDismiss()
    fun onAggregationConfirm()
}