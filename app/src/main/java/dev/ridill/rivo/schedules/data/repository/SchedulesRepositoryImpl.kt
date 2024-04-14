package dev.ridill.rivo.schedules.data.repository

import androidx.room.withTransaction
import dev.ridill.rivo.core.data.db.RivoDatabase
import dev.ridill.rivo.core.domain.service.ReceiverService
import dev.ridill.rivo.core.domain.util.DateUtil
import dev.ridill.rivo.schedules.data.local.SchedulesDao
import dev.ridill.rivo.schedules.data.toEntity
import dev.ridill.rivo.schedules.data.toSchedule
import dev.ridill.rivo.schedules.domain.model.Schedule
import dev.ridill.rivo.schedules.domain.model.ScheduleRepeatMode
import dev.ridill.rivo.schedules.domain.repository.SchedulesRepository
import dev.ridill.rivo.schedules.domain.scheduleReminder.ScheduleReminder
import dev.ridill.rivo.transactions.domain.repository.TransactionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalDateTime

class SchedulesRepositoryImpl(
    private val db: RivoDatabase,
    private val dao: SchedulesDao,
    private val txRepo: TransactionRepository,
    private val scheduler: ScheduleReminder,
    private val receiverService: ReceiverService
) : SchedulesRepository {
    override suspend fun getScheduleById(id: Long): Schedule? =
        withContext(Dispatchers.IO) {
            dao.getScheduleById(id)?.toSchedule()
        }

    override fun getNextReminderFromDate(
        date: LocalDate,
        repeatMode: ScheduleRepeatMode
    ): LocalDate? = when (repeatMode) {
        ScheduleRepeatMode.NO_REPEAT -> null
        ScheduleRepeatMode.WEEKLY -> date.plusWeeks(1)
        ScheduleRepeatMode.MONTHLY -> date.plusMonths(1)
        ScheduleRepeatMode.BI_MONTHLY -> date.plusMonths(2)
        ScheduleRepeatMode.YEARLY -> date.plusYears(1)
    }

    override fun getPrevReminderFromDate(
        date: LocalDate,
        repeatMode: ScheduleRepeatMode
    ): LocalDate? = when (repeatMode) {
        ScheduleRepeatMode.NO_REPEAT -> null
        ScheduleRepeatMode.WEEKLY -> date.minusWeeks(1)
        ScheduleRepeatMode.MONTHLY -> date.minusMonths(1)
        ScheduleRepeatMode.BI_MONTHLY -> date.minusMonths(2)
        ScheduleRepeatMode.YEARLY -> date.minusYears(1)
    }

    override suspend fun saveScheduleAndSetReminder(schedule: Schedule) {
        withContext(Dispatchers.IO) {
            val insertedId = dao.insert(schedule.toEntity()).first()
                .takeIf { it > RivoDatabase.DEFAULT_ID_LONG }
                ?: schedule.id
            scheduler.setReminder(
                schedule.copy(id = insertedId)
            )
            receiverService.toggleBootAndTimeSetReceivers(true)
        }
    }

    override suspend fun createTransactionForScheduleAndSetNextReminder(
        schedule: Schedule,
        dateTime: LocalDateTime
    ) = withContext(Dispatchers.IO) {
        db.withTransaction {
            txRepo.saveTransaction(
                amount = schedule.amount,
                note = schedule.note,
                timestamp = dateTime,
                type = schedule.type,
                tagId = schedule.tagId,
                folderId = schedule.folderId,
                scheduleId = schedule.id,
                excluded = false
            )
            val nextReminderDate = schedule.nextReminderDate
                ?.let { getNextReminderFromDate(it, schedule.repeatMode) }
            saveScheduleAndSetReminder(
                schedule = schedule.copy(
                    nextReminderDate = nextReminderDate,
                    lastPaidDate = dateTime.toLocalDate()
                )
            )
        }
    }

    override suspend fun getLastTransactionTimestampForSchedule(id: Long): LocalDateTime? =
        withContext(Dispatchers.IO) { dao.getLastTransactionTimestampForSchedule(id) }

    override suspend fun deleteScheduleById(id: Long) = withContext(Dispatchers.IO) {
        val entity = dao.getScheduleById(id) ?: return@withContext
        cancelSchedule(entity.toSchedule())
        dao.delete(entity)
    }

    override suspend fun cancelSchedule(schedule: Schedule) {
        scheduler.cancel(schedule)
    }

    override suspend fun setAllFutureScheduleReminders() = withContext(Dispatchers.IO) {
        dao.getAllSchedulesAfterDate(DateUtil.dateNow())
            .forEach { entity ->
                scheduler.setReminder(entity.toSchedule())
            }
    }
}