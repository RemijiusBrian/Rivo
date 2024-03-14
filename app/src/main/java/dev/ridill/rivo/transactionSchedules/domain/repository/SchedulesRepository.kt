package dev.ridill.rivo.transactionSchedules.domain.repository

import dev.ridill.rivo.transactionSchedules.domain.model.TxSchedule
import java.time.LocalDate

interface SchedulesRepository {
    suspend fun getScheduleById(id: Long): TxSchedule?
    suspend fun updateNextPaymentDateForScheduleById(id: Long, nextDate: LocalDate)
    suspend fun saveSchedule(schedule: TxSchedule): Long
    suspend fun setScheduleReminder(schedule: TxSchedule)
    suspend fun saveScheduleAndSetReminder(schedule: TxSchedule)
    suspend fun deleteScheduleById(id: Long)
    suspend fun cancelSchedule(schedule: TxSchedule)
    suspend fun setAllScheduleReminders()
}