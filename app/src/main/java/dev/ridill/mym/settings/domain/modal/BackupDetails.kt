package dev.ridill.mym.settings.domain.modal

import java.time.LocalDateTime

data class BackupDetails(
    val name: String,
    val id: String,
    val backupDateTime: LocalDateTime?
)