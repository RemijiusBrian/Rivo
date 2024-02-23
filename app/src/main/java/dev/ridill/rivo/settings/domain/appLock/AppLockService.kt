package dev.ridill.rivo.settings.domain.appLock

import android.app.Service
import android.content.Intent
import android.os.IBinder
import dagger.hilt.android.AndroidEntryPoint
import dev.ridill.rivo.core.data.preferences.PreferencesManager
import dev.ridill.rivo.core.domain.util.logI
import dev.ridill.rivo.settings.domain.notification.AppLockNotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class AppLockService : Service() {

    private val serviceScope = CoroutineScope(SupervisorJob())
    private var timerJob: Job? = null

    @Inject
    lateinit var notificationHelper: AppLockNotificationHelper

    @Inject
    lateinit var preferencesManager: PreferencesManager

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        logI { "App lock service onStartCommand - ${intent?.action}" }
        setForeground()
        when (intent?.action) {
            Action.START_SERVICE.name -> initService()
            Action.START_AUTO_LOCK_TIMER.name -> startTimer()
            Action.STOP_AUTO_LOCK_TIMER.name -> stopTimer()
            Action.LOCK_APP_IMMEDIATELY.name -> lockAppImmediate()
            Action.STOP_SERVICE.name -> stopService()
        }

        return START_STICKY
    }

    private fun initService() {
        // Some Basic Init
        resetTimerJob()
        logI { "Service Started" }
    }

    private fun startTimer() {
        logI { "Starting app lock timer" }
        timerJob?.cancel()
        timerJob = serviceScope.launch {
            val interval = preferencesManager.preferences.first().appAutoLockInterval
            delay(interval.duration)
            logI { "Locking app after auto lock timer" }
            preferencesManager.updateAppLocked(true)
            stopSelf()
        }
    }

    private fun stopTimer() {
        logI { "Stopping app lock timer" }
        resetTimerJob()
    }

    private fun lockAppImmediate() = serviceScope.launch {
        logI { "Locking app and terminating service" }
        preferencesManager.updateAppLocked(true)
    }

    private fun stopService() {
        logI { "Stopping service" }
        stopSelf()
    }

    private fun setForeground() {
        startForeground(
            NOTIFICATION_ID, notificationHelper.getForegroundNotification().build()
        )
    }

    private fun resetTimerJob() {
        timerJob?.cancel()
        timerJob = null
    }

    override fun onCreate() {
        super.onCreate()
        setForeground()
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        resetTimerJob()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    enum class Action {
        START_SERVICE,
        START_AUTO_LOCK_TIMER,
        STOP_AUTO_LOCK_TIMER,
        LOCK_APP_IMMEDIATELY,
        STOP_SERVICE
    }
}

private const val NOTIFICATION_ID = 6545