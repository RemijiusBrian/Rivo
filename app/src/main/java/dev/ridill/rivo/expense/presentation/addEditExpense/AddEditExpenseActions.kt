package dev.ridill.rivo.expense.presentation.addEditExpense

import androidx.compose.ui.graphics.Color
import java.time.LocalDateTime

interface AddEditExpenseActions {
    fun onAmountChange(value: String)
    fun onNoteInputFocused()
    fun onNoteChange(value: String)
    fun onRecommendedAmountClick(amount: Long)
    fun onTagClick(tagId: Long)
    fun onExpenseTimestampClick()
    fun onExpenseTimestampSelectionDismiss()
    fun onExpenseTimestampSelectionConfirm(dateTime: LocalDateTime)
    fun onExpenseExclusionToggle(excluded: Boolean)
    fun onSaveClick()
    fun onDeleteClick()
    fun onDeleteDismiss()
    fun onDeleteConfirm()
    fun onNewTagClick()
    fun onNewTagNameChange(value: String)
    fun onNewTagColorSelect(color: Color)
    fun onNewTagExclusionChange(excluded: Boolean)
    fun onNewTagInputDismiss()
    fun onNewTagInputConfirm()
}