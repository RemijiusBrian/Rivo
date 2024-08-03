package dev.ridill.rivo.transactions.presentation.addEditTransaction

import dev.ridill.rivo.core.domain.util.DateUtil
import dev.ridill.rivo.core.ui.util.UiText
import dev.ridill.rivo.schedules.domain.model.ScheduleRepeatMode
import dev.ridill.rivo.transactions.domain.model.TransactionType
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime

data class AddEditTransactionState(
    val isLoading: Boolean = false,
    val transactionType: TransactionType = TransactionType.DEBIT,
    val amountRecommendations: List<Long> = emptyList(),
    val timestamp: LocalDateTime = DateUtil.now(),
    val showDatePicker: Boolean = false,
    val showTimePicker: Boolean = false,
    val isTransactionExcluded: Boolean = false,
    val selectedTagId: Long? = null,
    val showNewTagInput: Boolean = false,
    val newTagError: UiText? = null,
    val showDeleteConfirmation: Boolean = false,
    val linkedFolderName: String? = null,
    val isScheduleTxMode: Boolean = false,
    val selectedRepeatMode: ScheduleRepeatMode = ScheduleRepeatMode.NO_REPEAT,
    val showRepeatModeSelection: Boolean = false
) {
    val timestampUtc: ZonedDateTime
        get() = timestamp.atZone(ZoneId.of(ZoneOffset.UTC.id))
}