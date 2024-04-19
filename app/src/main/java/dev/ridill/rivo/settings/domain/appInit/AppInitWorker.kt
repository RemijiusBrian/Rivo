package dev.ridill.rivo.settings.domain.appInit

import android.content.Context
import android.content.pm.ServiceInfo
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import dev.ridill.rivo.R
import dev.ridill.rivo.core.domain.notification.NotificationHelper
import dev.ridill.rivo.di.AppInitFeature
import dev.ridill.rivo.settings.domain.repositoty.AppInitRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@HiltWorker
class AppInitWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted params: WorkerParameters,
    private val repo: AppInitRepository,
    @AppInitFeature private val notificationHelper: NotificationHelper<Unit>
) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        startForeground()
        try {
            repo.initCurrenciesList()
            Result.success()
        } catch (t: Throwable) {
            Result.failure()
        }
    }

    private suspend fun startForeground() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            setForeground(
                ForegroundInfo(
                    AppInitWorkManager.APP_INIT_NOTIFICATION_ID.hashCode(),
                    notificationHelper.buildBaseNotification()
                        .setContentTitle(
                            appContext.getString(
                                R.string.initializing_app,
                                appContext.getString(R.string.app_name)
                            )
                        )
                        .setProgress(100, 0, true)
                        .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
                        .build(),
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
                )
            )
        } else {
            setForeground(
                ForegroundInfo(
                    AppInitWorkManager.APP_INIT_NOTIFICATION_ID.hashCode(),
                    notificationHelper.buildBaseNotification()
                        .setContentTitle(
                            appContext.getString(
                                R.string.initializing_app,
                                appContext.getString(R.string.app_name)
                            )
                        )
                        .setProgress(100, 0, true)
                        .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
                        .build()
                )
            )
        }
    }
}