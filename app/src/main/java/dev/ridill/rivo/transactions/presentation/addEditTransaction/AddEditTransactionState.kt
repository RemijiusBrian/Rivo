package dev.ridill.rivo.transactions.presentation.addEditTransaction

import dev.ridill.rivo.core.domain.model.DateTimePickerMode
import dev.ridill.rivo.core.domain.util.DateUtil
import dev.ridill.rivo.core.ui.util.UiText
import dev.ridill.rivo.schedules.domain.model.ScheduleRepeatMode
import dev.ridill.rivo.transactions.domain.model.AmountTransformation
import dev.ridill.rivo.transactions.domain.model.TransactionType
import java.time.LocalDateTime

data class AddEditTransactionState(
    val isLoading: Boolean = false,
    val transactionType: TransactionType = TransactionType.DEBIT,
    val amountRecommendations: List<Long> = emptyList(),
    val amountTransformation: AmountTransformation = AmountTransformation.DIVIDE_BY,
    val showAmountTransformationInput: Boolean = false,
    val timestamp: LocalDateTime = DateUtil.now(),
    val currentPickerMode: DateTimePickerMode = DateTimePickerMode.DATE_PICKER,
    val showDatePicker: Boolean = false,
    val showTimePicker: Boolean = false,
    val isTransactionExcluded: Boolean = false,
    val selectedTagId: Long? = null,
    val showNewTagInput: Boolean = false,
    val newTagError: UiText? = null,
    val showDeleteConfirmation: Boolean = false,
    val linkedFolderName: String? = null,
    val showFolderSelection: Boolean = false,
    val isScheduleTxMode: Boolean = false,
    val selectedRepeatMode: ScheduleRepeatMode = ScheduleRepeatMode.NO_REPEAT,
    val showRepeatModeSelection: Boolean = false
)