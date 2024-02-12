package dev.ridill.rivo.settings.domain.appLock

import android.app.Service
import android.content.Intent
import android.os.IBinder
import dagger.hilt.android.AndroidEntryPoint
import dev.ridill.rivo.core.data.preferences.PreferencesManager
import dev.ridill.rivo.settings.domain.notification.AppLockNotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.random.Random

@AndroidEntryPoint
class AppLockTimerService : Service() {

    private val serviceScope = CoroutineScope(SupervisorJob())

    @Inject
    lateinit var notificationHelper: AppLockNotificationHelper

    @Inject
    lateinit var preferencesManager: PreferencesManager

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(
            Random.nextInt(), notificationHelper.getForegroundNotification().build()
        )
        when (intent?.action) {
            Action.START_TIMER.toString() -> startTimer()
            Action.STOP_TIMER.toString() -> stopSelf()
            Action.IMMEDIATE_LOCK.toString() -> lockAppImmediate()
        }

        return START_STICKY
    }

    private fun lockAppImmediate() = serviceScope.launch {
        preferencesManager.updateAppLocked(true)
        stopSelf()
    }

    private fun startTimer() = serviceScope.launch {
        val interval = preferencesManager.preferences.first().appAutoLockInterval
        delay(interval.duration)
        preferencesManager.updateAppLocked(true)
        stopSelf()
    }

    override fun onCreate() {
        super.onCreate()
        startForeground(
            Random.nextInt(), notificationHelper.getForegroundNotification().build()
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    enum class Action {
        START_TIMER,
        STOP_TIMER,
        IMMEDIATE_LOCK
    }
}