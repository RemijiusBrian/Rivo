package dev.ridill.rivo.transactions.presentation.addEditTransaction

import dev.ridill.rivo.schedules.domain.model.ScheduleRepeatMode
import dev.ridill.rivo.transactions.domain.model.AddEditTxOption
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
    fun onDeleteClick()
    fun onDeleteDismiss()
    fun onDeleteConfirm()
    fun onRemoveFromFolderClick()
    fun onAddEditOptionSelect(option: AddEditTxOption)
    fun onCancelSchedulingClick()
    fun onRepeatModeClick()
    fun onRepeatModeDismiss()
    fun onRepeatModeSelect(repeatMode: ScheduleRepeatMode)
    fun onSaveClick()
}