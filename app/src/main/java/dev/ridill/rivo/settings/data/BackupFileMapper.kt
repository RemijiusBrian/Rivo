package dev.ridill.rivo.settings.data

import dev.ridill.rivo.settings.data.remote.dto.GDriveFileDto
import dev.ridill.rivo.settings.domain.backup.DB_BACKUP_FILE_NAME
import dev.ridill.rivo.settings.domain.modal.BackupDetails

fun GDriveFileDto.toBackupDetails(): BackupDetails = BackupDetails(
    name = name,
    id = id,
    timestamp = name.removeSuffix("-$DB_BACKUP_FILE_NAME")
)