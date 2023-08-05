package dev.ridill.mym.expense.presentation.addEditExpense

import androidx.compose.ui.graphics.Color

interface AddEditExpenseActions {
    fun onAmountChange(value: String)
    fun onNoteInputFocused()
    fun onNoteChange(value: String)
    fun onRecommendedAmountClick(amount: Long)
    fun onTagClick(tagId: String)
    fun onSave()
    fun onDeleteClick()
    fun onDeleteDismiss()
    fun onDeleteConfirm()
    fun onNewTagClick()
    fun onNewTagNameChange(value: String)
    fun onNewTagColorSelect(color: Color)
    fun onNewTagInputDismiss()
    fun onNewTagInputConfirm()
}