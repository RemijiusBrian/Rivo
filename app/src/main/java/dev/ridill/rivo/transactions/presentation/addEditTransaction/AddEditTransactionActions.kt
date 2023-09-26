package dev.ridill.rivo.transactions.presentation.addEditTransaction

import androidx.compose.ui.graphics.Color
import java.time.LocalDateTime

interface AddEditTransactionActions {
    fun onAmountChange(value: String)
    fun onNoteInputFocused()
    fun onNoteChange(value: String)
    fun onRecommendedAmountClick(amount: Long)
    fun onTagClick(tagId: Long)
    fun onTransactionTimestampClick()
    fun onTransactionTimestampSelectionDismiss()
    fun onTransactionTimestampSelectionConfirm(dateTime: LocalDateTime)
    fun onTransactionExclusionToggle(excluded: Boolean)
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