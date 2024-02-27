package dev.ridill.rivo.settings.domain.backup

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import dev.ridill.rivo.core.domain.model.Resource
import dev.ridill.rivo.settings.domain.repositoty.BackupRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@HiltWorker
class GDriveDataBackupWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted params: WorkerParameters,
    private val repo: BackupRepository
) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        when (val resource = repo.performAppDataBackup()) {
            is Resource.Error -> {
                Result.failure(
                    workDataOf(
                        BackupWorkManager.KEY_MESSAGE to resource.message?.asString(appContext)
                    )
                )
            }

            is Resource.Success -> {
                Result.success(
                    workDataOf(
                        BackupWorkManager.KEY_MESSAGE to resource.message?.asString(appContext)
                    )
                )
            }
        }
    }
}