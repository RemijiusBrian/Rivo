package dev.ridill.rivo.schedules.domain.repository

import dev.ridill.rivo.core.domain.util.DateUtil
import dev.ridill.rivo.schedules.domain.model.Schedule
import dev.ridill.rivo.schedules.domain.model.ScheduleRepeatMode
import java.time.LocalDate
import java.time.LocalDateTime

interface SchedulesRepository {
    suspend fun getScheduleById(id: Long): Schedule?
    fun getNextReminderFromDate(date: LocalDate, repeatMode: ScheduleRepeatMode): LocalDate?
    fun getPrevReminderFromDate(date: LocalDate, repeatMode: ScheduleRepeatMode): LocalDate?
    suspend fun saveScheduleAndSetReminder(schedule: Schedule)
    suspend fun createTransactionForScheduleAndSetNextReminder(
        schedule: Schedule,
        dateTime: LocalDateTime = DateUtil.now()
    )

    suspend fun getLastTransactionTimestampForSchedule(id: Long): LocalDateTime?
    suspend fun deleteScheduleById(id: Long)
    suspend fun cancelSchedule(schedule: Schedule)
    suspend fun setAllFutureScheduleReminders()
    suspend fun deleteSchedulesByIds(ids: Set<Long>)
}