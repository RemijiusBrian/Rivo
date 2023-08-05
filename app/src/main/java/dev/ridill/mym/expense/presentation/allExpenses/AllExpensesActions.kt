package dev.ridill.mym.expense.presentation.allExpenses

import dev.ridill.mym.expense.domain.ExpenseBulkOperation
import java.time.Month

interface AllExpensesActions {
    fun onMonthSelect(month: Month)
    fun onYearSelect(year: Int)
    fun onTagClick(tag: String)
    fun onNewTagClick()
    fun onExpenseLongClick(id: Long)
    fun onExpenseClick(id: Long)
    fun onSelectionStateChange()
    fun onDismissMultiSelectionMode()
    fun onExpenseBulkOperationClick(operation: ExpenseBulkOperation)
    fun onDeleteDismiss()
    fun onDeleteConfirm()
}