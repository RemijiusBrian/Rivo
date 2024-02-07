package dev.ridill.rivo.settings.domain.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import dev.ridill.rivo.settings.domain.appLock.AppLockTimerService

class LockAppImmediateReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val serviceIntent = Intent(context, AppLockTimerService::class.java).apply {
            action = AppLockTimerService.Action.IMMEDIATE_LOCK.name
        }
        context?.let { ContextCompat.startForegroundService(it, serviceIntent) }
    }
}