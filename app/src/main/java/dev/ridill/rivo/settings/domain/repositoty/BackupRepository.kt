package dev.ridill.rivo.settings.domain.repositoty

import dev.ridill.rivo.core.domain.model.Resource
import dev.ridill.rivo.core.domain.model.SimpleResource
import dev.ridill.rivo.settings.domain.modal.BackupDetails

interface BackupRepository {
    suspend fun checkForBackup(): Resource<BackupDetails>
    suspend fun performAppDataBackup(): SimpleResource
    suspend fun performAppDataRestore(details: BackupDetails): SimpleResource
}