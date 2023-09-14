package dev.ridill.mym.expense.presentation.allExpenses

import androidx.compose.ui.graphics.Color
import dev.ridill.mym.expense.domain.model.ExpenseOption
import dev.ridill.mym.expense.domain.model.ExpenseTag
import java.time.Month

interface AllExpensesActions {
    fun onMonthSelect(month: Month)
    fun onYearSelect(year: Int)
    fun onTagClick(tag: ExpenseTag)
    fun onNewTagClick()
    fun onNewTagNameChange(value: String)
    fun onNewTagColorSelect(color: Color)
    fun onNewTagInputDismiss()
    fun onNewTagInputConfirm()
    fun onToggleShowExcludedExpenses(value: Boolean)
    fun onExpenseLongClick(id: Long)
    fun onExpenseClick(id: Long)
    fun onSelectionStateChange()
    fun onDismissMultiSelectionMode()
    fun onExpenseOptionClick(option: ExpenseOption)
    fun onDeleteSelectedExpensesClick()
    fun onDeleteExpenseDismiss()
    fun onDeleteExpenseConfirm()
    fun onEditTagClick(tag: ExpenseTag)
    fun onDeleteTagClick(tagId: Long)
    fun onDeleteTagDismiss()
    fun onDeleteTagConfirm()
    fun onDeleteTagWithExpensesClick()
}