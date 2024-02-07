package dev.ridill.rivo.settings.presentation.security

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import dev.ridill.rivo.settings.domain.appLock.AppLockTimerService

class AppLockManager(
    private val context: Context
) {
    fun startAppLockTimerService() {
        val serviceIntent = Intent(context, AppLockTimerService::class.java).apply {
            action = AppLockTimerService.Action.START_TIMER.name
        }
        ContextCompat.startForegroundService(context, serviceIntent)
    }

    fun stopAppLockTimer() {
        val serviceIntent = Intent(context, AppLockTimerService::class.java)
        context.stopService(serviceIntent)
    }
}