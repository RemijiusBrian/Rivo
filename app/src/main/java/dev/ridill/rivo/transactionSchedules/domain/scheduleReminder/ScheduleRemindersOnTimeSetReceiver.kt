package dev.ridill.rivo.transactionSchedules.domain.scheduleReminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dagger.hilt.android.AndroidEntryPoint
import dev.ridill.rivo.di.ApplicationScope
import dev.ridill.rivo.transactionSchedules.domain.repository.SchedulesRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ScheduleRemindersOnTimeSetReceiver : BroadcastReceiver() {

    @ApplicationScope
    @Inject
    lateinit var applicationScope: CoroutineScope

    @Inject
    lateinit var repo: SchedulesRepository

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action != Intent.ACTION_TIME_CHANGED) return
        applicationScope.launch {
            repo.setAllFutureScheduleReminders()
        }
    }
}