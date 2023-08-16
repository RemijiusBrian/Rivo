package dev.ridill.mym.settings.data

import dev.ridill.mym.settings.data.remote.dto.GDriveFileDto
import dev.ridill.mym.settings.domain.backup.BACKUP_FILE_NAME
import dev.ridill.mym.settings.domain.modal.BackupDetails

fun GDriveFileDto.toBackupDetails(): BackupDetails = BackupDetails(
    name = name,
    id = id,
    timestamp = name.removeSuffix("-$BACKUP_FILE_NAME")
)