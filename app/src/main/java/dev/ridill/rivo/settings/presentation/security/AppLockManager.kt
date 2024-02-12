package dev.ridill.rivo.settings.presentation.security

import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import androidx.core.content.ContextCompat
import dev.ridill.rivo.settings.domain.appLock.AppLockTimerService

class AppLockManager(
    private val context: Context
) {
    fun startAppLockTimerService() {
        val serviceIntent = Intent(context, AppLockTimerService::class.java).apply {
            action = AppLockTimerService.Action.START_TIMER.name
        }
        Handler(Looper.getMainLooper()).postDelayed(
            {
                ContextCompat.startForegroundService(
                    context,
                    serviceIntent
                )
            },
            500
        )
    }

    fun stopAppLockTimer() {
        val serviceIntent = Intent(context, AppLockTimerService::class.java)
        Handler(Looper.getMainLooper()).postDelayed(
            {
                context.stopService(serviceIntent)
            },
            500
        )
    }
}