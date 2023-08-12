package dev.ridill.mym.settings.domain

interface BackupRepository {
    suspend fun performAppDataBackup()
}