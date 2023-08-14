package dev.ridill.mym.settings.domain.backup

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import dev.ridill.mym.core.domain.model.Resource
import dev.ridill.mym.settings.domain.repositoty.BackupRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@HiltWorker
class GDriveDataRestoreWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val repo: BackupRepository
) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        when (repo.performAppDataRestore()) {
            is Resource.Error -> Result.failure()
            is Resource.Success -> Result.success()
        }
    }
}