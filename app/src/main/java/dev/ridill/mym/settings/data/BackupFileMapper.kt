package dev.ridill.mym.settings.data

import dev.ridill.mym.core.domain.util.DateUtil
import dev.ridill.mym.settings.data.remote.dto.GDriveFileDto
import dev.ridill.mym.settings.domain.backup.BACKUP_FILE_NAME
import dev.ridill.mym.settings.domain.modal.BackupDetails

fun GDriveFileDto.toBackupDetails(): BackupDetails {
    return BackupDetails(
        name = name,
        id = id,
        backupDateTime = DateUtil.parse(name.removeSuffix("-$BACKUP_FILE_NAME"))
    )
}