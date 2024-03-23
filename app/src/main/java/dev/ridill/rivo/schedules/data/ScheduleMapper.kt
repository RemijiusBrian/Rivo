package dev.ridill.rivo.schedules.data

import dev.ridill.rivo.core.data.db.RivoDatabase
import dev.ridill.rivo.core.domain.util.DateUtil
import dev.ridill.rivo.core.ui.util.UiText
import dev.ridill.rivo.schedules.data.local.entity.ScheduleEntity
import dev.ridill.rivo.schedules.data.local.relation.ScheduleWithLastTransactionRelation
import dev.ridill.rivo.schedules.domain.model.UpcomingSchedule
import dev.ridill.rivo.schedules.domain.model.Schedule
import dev.ridill.rivo.schedules.domain.model.ScheduleListItem
import dev.ridill.rivo.schedules.domain.model.ScheduleRepeatMode
import dev.ridill.rivo.transactions.domain.model.Transaction
import dev.ridill.rivo.transactions.domain.model.TransactionType
import java.time.LocalDate
import java.time.LocalDateTime

fun ScheduleEntity.toSchedule(): Schedule = Schedule(
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

fun Schedule.toEntity(): ScheduleEntity = ScheduleEntity(
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
): ScheduleListItem = ScheduleListItem(
    id = schedule.id,
    amount = schedule.amount,
    note = schedule.note,
    nextReminderDate = schedule.nextReminderDate,
    lastPaymentTimestamp = transactionEntity?.timestamp,
    canMarkPaid = schedule.nextReminderDate?.isAfter(dateNow) == true
            && schedule.nextReminderDate.monthValue == dateNow.monthValue
)

fun ScheduleEntity.toActiveSchedule(): UpcomingSchedule = UpcomingSchedule(
    id = id,
    note = note?.let { UiText.DynamicString(it) }
        ?: UiText.StringResource(TransactionType.valueOf(typeName).labelRes),
    amount = amount,
    dueDate = nextReminderDate ?: DateUtil.dateNow()
)