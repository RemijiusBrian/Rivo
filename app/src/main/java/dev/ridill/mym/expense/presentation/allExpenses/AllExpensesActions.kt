package dev.ridill.mym.expense.presentation.allExpenses

import androidx.compose.ui.graphics.Color
import dev.ridill.mym.expense.domain.model.ExpenseBulkOperation
import java.time.Month

interface AllExpensesActions {
    fun onMonthSelect(month: Month)
    fun onYearSelect(year: Int)
    fun onTagClick(tag: String)
    fun onNewTagClick()
    fun onNewTagNameChange(value: String)
    fun onNewTagColorSelect(color: Color)
    fun onNewTagInputDismiss()
    fun onNewTagInputConfirm()
    fun onExpenseLongClick(id: Long)
    fun onExpenseClick(id: Long)
    fun onSelectionStateChange()
    fun onDismissMultiSelectionMode()
    fun onExpenseBulkOperationClick(operation: ExpenseBulkOperation)
    fun onDeleteExpenseDismiss()
    fun onDeleteExpenseConfirm()
    fun onDeleteTagClick(tagName: String)
    fun onDeleteTagDismiss()
    fun onDeleteTagConfirm()
    fun onDeleteTagWithExpensesClick()
}