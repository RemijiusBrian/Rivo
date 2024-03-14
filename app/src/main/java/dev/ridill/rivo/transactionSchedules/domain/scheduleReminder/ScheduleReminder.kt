package dev.ridill.rivo.transactionSchedules.domain.scheduleReminder

import dev.ridill.rivo.transactionSchedules.domain.model.Schedule

interface ScheduleReminder {
    fun setReminder(schedule: Schedule)
    fun cancel(transaction: Schedule)

    companion object {
        const val ACTION = "dev.ridill.rivo.SCHEDULE_REMINDER"
        const val EXTRA_SCHEDULE_ID = "EXTRA_SCHEDULE_ID"
    }
}