package dev.ridill.rivo.transactionSchedules.data

import dev.ridill.rivo.core.data.db.RivoDatabase
import dev.ridill.rivo.core.domain.util.DateUtil
import dev.ridill.rivo.transactionSchedules.data.local.entity.TxScheduleEntity
import dev.ridill.rivo.transactionSchedules.data.local.relation.ScheduleWithLastTransactionRelation
import dev.ridill.rivo.transactionSchedules.domain.model.ScheduleRepeatMode
import dev.ridill.rivo.transactionSchedules.domain.model.Schedule
import dev.ridill.rivo.transactionSchedules.domain.model.TxScheduleListItem
import dev.ridill.rivo.transactions.domain.model.Transaction
import dev.ridill.rivo.transactions.domain.model.TransactionType
import java.time.LocalDate
import java.time.LocalDateTime

fun TxScheduleEntity.toSchedule(): Schedule = Schedule(
    id = id,
    repeatMode = ScheduleRepeatMode.valueOf(repeatModeName),
    nextReminderDate = nextReminderDate,
    amount = amount,
    note = note,
    type = TransactionType.valueOf(typeName),
    tagId = tagId,
    folderId = folderId
)

fun Schedule.toTransaction(
    dateTime: LocalDateTime = DateUtil.now(),
    txId: Long = RivoDatabase.DEFAULT_ID_LONG
): Transaction = Transaction(
    id = txId,
    amount = amount.toString(),
    note = note.orEmpty(),
    timestamp = dateTime,
    type = type,
    tagId = tagId,
    folderId = folderId,
    excluded = false,
    scheduleId = id
)

fun Schedule.toEntity(): TxScheduleEntity = TxScheduleEntity(
    id = id,
    amount = amount,
    note = note,
    typeName = type.name,
    repeatModeName = repeatMode.name,
    tagId = tagId,
    folderId = folderId,
    nextReminderDate = nextReminderDate
)

fun ScheduleWithLastTransactionRelation.toScheduleListItem(
    dateNow: LocalDate
): TxScheduleListItem = TxScheduleListItem(
    id = schedule.id,
    amount = schedule.amount,
    note = schedule.note,
    nextReminderDate = schedule.nextReminderDate,
    lastPaymentTimestamp = transactionEntity?.timestamp,
    canMarkPaid = schedule.nextReminderDate?.isAfter(dateNow) == true
            && schedule.nextReminderDate.monthValue == dateNow.monthValue
)