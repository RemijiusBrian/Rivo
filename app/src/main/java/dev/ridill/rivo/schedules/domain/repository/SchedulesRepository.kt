package dev.ridill.rivo.schedules.domain.repository

import dev.ridill.rivo.schedules.domain.model.Schedule
import java.time.LocalDate

interface SchedulesRepository {
    suspend fun getScheduleById(id: Long): Schedule?
    suspend fun updateNextPaymentDateForScheduleById(id: Long, nextDate: LocalDate)
    suspend fun saveSchedule(schedule: Schedule): Long
    suspend fun setScheduleReminder(schedule: Schedule)
    suspend fun saveScheduleAndSetReminder(schedule: Schedule)
    suspend fun deleteScheduleById(id: Long)
    suspend fun cancelSchedule(schedule: Schedule)
    suspend fun setAllFutureScheduleReminders()
}