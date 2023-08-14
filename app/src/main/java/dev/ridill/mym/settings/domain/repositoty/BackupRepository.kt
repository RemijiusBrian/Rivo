package dev.ridill.mym.settings.domain.repositoty

import dev.ridill.mym.core.domain.model.SimpleResource

interface BackupRepository {
    suspend fun performAppDataBackup(): SimpleResource
    suspend fun performAppDataRestore(): SimpleResource
}