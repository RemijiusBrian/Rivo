package dev.ridill.rivo.settings.domain.backup

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.google.android.gms.auth.GoogleAuthException
import com.google.android.gms.auth.UserRecoverableAuthException
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import dev.ridill.rivo.R
import dev.ridill.rivo.core.domain.notification.NotificationHelper
import dev.ridill.rivo.core.domain.util.logE
import dev.ridill.rivo.core.domain.util.logI
import dev.ridill.rivo.di.BackupFeature
import dev.ridill.rivo.settings.data.repository.InvalidEncryptionPasswordThrowable
import dev.ridill.rivo.settings.domain.repositoty.BackupRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException

@HiltWorker
class GDriveDataBackupWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted params: WorkerParameters,
    private val repo: BackupRepository,
    @BackupFeature private val notificationHelper: NotificationHelper<String>,
    private val workManager: BackupWorkManager
) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            repo.performAppDataBackup()
            logI { "Backup Completed" }
            Result.success(
                workDataOf(
                    BackupWorkManager.KEY_MESSAGE to appContext.getString(R.string.backup_complete)
                )
            )
        } catch (t: InvalidEncryptionPasswordThrowable) {
            logE(t) { "InvalidEncryptionPasswordThrowable" }
            notificationHelper.postNotification(
                BackupWorkManager.BACKUP_WORKER_NOTIFICATION_ID.hashCode(),
                appContext.getString(R.string.error_invalid_encryption_password)
            )
            workManager.cancelPeriodicBackupWork()
            Result.failure(
                workDataOf(
                    BackupWorkManager.KEY_MESSAGE to appContext.getString(R.string.error_invalid_encryption_password)
                )
            )
        } catch (e: UserRecoverableAuthException) {
            logE(e) { "UserRecoverableAuthException" }
            notificationHelper.postNotification(
                BackupWorkManager.BACKUP_WORKER_NOTIFICATION_ID.hashCode(),
                appContext.getString(R.string.error_google_auth_failed)
            )
            workManager.cancelPeriodicBackupWork()
            Result.failure(
                workDataOf(
                    BackupWorkManager.KEY_MESSAGE to appContext.getString(R.string.error_google_auth_failed)
                )
            )
        } catch (e: GoogleAuthException) {
            logE(e) { "GoogleAuthException" }
            notificationHelper.postNotification(
                BackupWorkManager.BACKUP_WORKER_NOTIFICATION_ID.hashCode(),
                appContext.getString(R.string.error_google_auth_failed)
            )
            workManager.cancelPeriodicBackupWork()
            Result.failure(
                workDataOf(
                    BackupWorkManager.KEY_MESSAGE to appContext.getString(R.string.error_google_auth_failed)
                )
            )
        } catch (e: HttpException) {
            logE(e) { "HttpException" }
            resultForHttpCode(e.code())
        } catch (t: BackupCachingFailedThrowable) {
            logE(t) { "BackupCachingFailedThrowable" }
            Result.retry()
        } catch (t: Throwable) {
            logE(t) { "Throwable" }
            Result.retry()
        } finally {
            repo.tryClearLocalCache()
        }
    }

    private fun resultForHttpCode(code: Int): Result = when (code) {
        in listOf(400, 401, 404, 429) -> Result.failure()
        else -> Result.retry()
    }
}