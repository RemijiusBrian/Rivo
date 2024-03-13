package dev.ridill.rivo.transactionSchedules.data

import dev.ridill.rivo.core.data.db.RivoDatabase
import dev.ridill.rivo.core.domain.util.DateUtil
import dev.ridill.rivo.transactionSchedules.data.local.entity.TxScheduleEntity
import dev.ridill.rivo.transactionSchedules.data.local.relation.ScheduleWithLastPaidDateRelation
import dev.ridill.rivo.transactionSchedules.domain.model.ScheduleRepeatMode
import dev.ridill.rivo.transactionSchedules.domain.model.TxSchedule
import dev.ridill.rivo.transactionSchedules.domain.model.TxScheduleListItem
import dev.ridill.rivo.transactionSchedules.domain.model.TxScheduleStatus
import dev.ridill.rivo.transactions.domain.model.Transaction
import dev.ridill.rivo.transactions.domain.model.TransactionType
import java.time.LocalDate
import java.time.LocalDateTime

fun TxScheduleEntity.toSchedule(): TxSchedule = TxSchedule(
    id = id,
    repeatMode = ScheduleRepeatMode.valueOf(repeatModeName),
    nextReminderDate = nextReminderDate,
    amount = amount,
    note = note,
    type = TransactionType.valueOf(typeName)
)

fun TxSchedule.toTransaction(
    dateTime: LocalDateTime = DateUtil.now(),
    txId: Long = RivoDatabase.DEFAULT_ID_LONG
): Transaction = Transaction(
    id = txId,
    amount = amount.toString(),
    note = note.orEmpty(),
    timestamp = dateTime,
    type = type,
    tagId = null,
    folderId = null,
    excluded = false,
    scheduleId = id
)

fun TxSchedule.toEntity(): TxScheduleEntity = TxScheduleEntity(
    id = id,
    amount = amount,
    note = note,
    typeName = type.name,
    repeatModeName = repeatMode.name,
    nextReminderDate = nextReminderDate
)

fun ScheduleWithLastPaidDateRelation.toListItem(
    date: LocalDate
): TxScheduleListItem = TxScheduleListItem(
    id = id,
    amount = amount,
    note = note,
    nextReminderDate = nextReminderDate,
    lastPaymentTimestamp = lastPaymentTimestamp,
    status = when {
        nextReminderDate == null
                && lastPaymentTimestamp?.isBefore(
            date.plusDays(1).atStartOfDay()
        ) == true -> TxScheduleStatus.RETIRED

        lastPaymentTimestamp == null
                && nextReminderDate?.isAfter(date) == true -> TxScheduleStatus.UPCOMING

        lastPaymentTimestamp?.month == nextReminderDate?.month
                && lastPaymentTimestamp?.year == nextReminderDate?.year -> TxScheduleStatus.SETTLED

        else -> TxScheduleStatus.DUE
    }
)