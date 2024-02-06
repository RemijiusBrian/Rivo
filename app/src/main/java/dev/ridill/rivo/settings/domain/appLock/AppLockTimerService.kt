package dev.ridill.rivo.settings.domain.appLock

import android.app.Service
import android.content.Intent
import android.content.SharedPreferences
import android.os.IBinder
import dev.ridill.rivo.core.data.preferences.PreferencesManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

class AppLockTimerService : Service() {

    private val serviceScope = CoroutineScope(SupervisorJob())

    @Inject
    lateinit var preferencesManager: PreferencesManager

    @Inject
    lateinit var sharedPreferences: SharedPreferences

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            Action.START.toString() -> startTimer()
            Action.LOCK.toString() -> lockApp()
        }

        return START_STICKY
    }

    private fun lockApp() {
        
    }

    private fun startTimer() {
        serviceScope.launch {
            val interval = preferencesManager.preferences.first().appAutoLockInterval
            delay(interval.duration)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    enum class Action {
        START,
        LOCK
    }
}