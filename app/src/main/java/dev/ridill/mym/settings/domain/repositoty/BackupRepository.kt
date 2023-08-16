package dev.ridill.mym.settings.domain.repositoty

import dev.ridill.mym.core.domain.model.Resource
import dev.ridill.mym.core.domain.model.SimpleResource
import dev.ridill.mym.settings.domain.modal.BackupDetails

interface BackupRepository {
    suspend fun checkForBackup(): Resource<BackupDetails>
    suspend fun performAppDataBackup(): SimpleResource
    suspend fun performAppDataRestore(details: BackupDetails): SimpleResource
}