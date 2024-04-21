package dev.ridill.rivo.transactions.presentation.addEditTransaction

import androidx.compose.ui.graphics.Color
import dev.ridill.rivo.folders.domain.model.Folder
import dev.ridill.rivo.schedules.domain.model.ScheduleRepeatMode
import dev.ridill.rivo.transactions.domain.model.AddEditTxOption
import dev.ridill.rivo.transactions.domain.model.AmountTransformation
import dev.ridill.rivo.transactions.domain.model.TransactionType

interface AddEditTransactionActions {
    fun onAmountChange(value: String)
    fun onNoteInputFocused()
    fun onNoteChange(value: String)
    fun onRecommendedAmountClick(amount: Long)
    fun onTagClick(tagId: Long)
    fun onTimestampClick()
    fun onDateSelectionDismiss()
    fun onDateSelectionConfirm(millis: Long)
    fun onPickTimeClick()
    fun onTimeSelectionDismiss()
    fun onTimeSelectionConfirm(hour: Int, minute: Int)
    fun onPickDateClick()
    fun onTypeChange(type: TransactionType)
    fun onExclusionToggle(excluded: Boolean)
    fun onTransformAmountClick()
    fun onTransformAmountDismiss()
    fun onAmountTransformationSelect(criteria: AmountTransformation)
    fun onAmountTransformationConfirm(value: String)
    fun onDeleteClick()
    fun onDeleteDismiss()
    fun onDeleteConfirm()
    fun onNewTagClick()
    fun onNewTagNameChange(value: String)
    fun onNewTagColorSelect(color: Color)
    fun onNewTagExclusionChange(excluded: Boolean)
    fun onNewTagInputDismiss()
    fun onNewTagInputConfirm()
    fun onAddToFolderClick()
    fun onRemoveFromFolderClick()
    fun onFolderSearchQueryChange(query: String)
    fun onFolderSelectionDismiss()
    fun onFolderSelect(folder: Folder)
    fun onCreateFolderClick()
    fun onAddEditOptionSelect(option: AddEditTxOption)
    fun onCancelSchedulingClick()
    fun onRepeatModeClick()
    fun onRepeatModeDismiss()
    fun onRepeatModeSelect(repeatMode: ScheduleRepeatMode)
    fun onSaveClick()
}