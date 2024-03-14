package dev.ridill.rivo.transactionSchedules.data.repository

import dev.ridill.rivo.core.data.db.RivoDatabase
import dev.ridill.rivo.core.domain.util.DateUtil
import dev.ridill.rivo.transactionSchedules.data.local.TxSchedulesDao
import dev.ridill.rivo.transactionSchedules.data.toEntity
import dev.ridill.rivo.transactionSchedules.data.toSchedule
import dev.ridill.rivo.transactionSchedules.domain.model.TxSchedule
import dev.ridill.rivo.transactionSchedules.domain.repository.SchedulesRepository
import dev.ridill.rivo.transactionSchedules.domain.transactionScheduler.TransactionScheduler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate

class SchedulesRepositoryImpl(
    private val dao: TxSchedulesDao,
    private val scheduler: TransactionScheduler
) : SchedulesRepository {
    override suspend fun getScheduleById(id: Long): TxSchedule? =
        withContext(Dispatchers.IO) {
            dao.getScheduleById(id)?.toSchedule()
        }

    override suspend fun updateNextPaymentDateForScheduleById(id: Long, nextDate: LocalDate) =
        withContext(Dispatchers.IO) {
            dao.updateNextReminderDateForScheduleById(id = id, nextDate = nextDate)
        }

    override suspend fun saveSchedule(schedule: TxSchedule) = withContext(Dispatchers.IO) {
        dao.insert(schedule.toEntity()).first()
    }

    override suspend fun setScheduleReminder(schedule: TxSchedule) {
        scheduler.schedule(schedule)
    }

    override suspend fun saveScheduleAndSetReminder(schedule: TxSchedule) {
        withContext(Dispatchers.IO) {
            val insertedId = saveSchedule(schedule)
                .takeIf { it > RivoDatabase.DEFAULT_ID_LONG }
                ?: schedule.id
            setScheduleReminder(
                schedule = schedule.copy(id = insertedId)
            )
        }
    }

    override suspend fun deleteScheduleById(id: Long) = withContext(Dispatchers.IO) {
        val entity = dao.getScheduleById(id) ?: return@withContext
        cancelSchedule(entity.toSchedule())
        dao.delete(entity)
    }

    override suspend fun cancelSchedule(schedule: TxSchedule) {
        scheduler.cancel(schedule)
    }

    override suspend fun setAllScheduleReminders() = withContext(Dispatchers.IO) {
        dao.getAllSchedulesAfterDate(DateUtil.dateNow())
            .forEach { entity ->
                scheduler.schedule(entity.toSchedule())
            }
    }
}