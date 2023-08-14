package dev.ridill.mym.settings.data

import dev.ridill.mym.core.domain.util.DateUtil
import dev.ridill.mym.settings.data.remote.dto.GDriveFileDto
import dev.ridill.mym.settings.domain.modal.BackupDetails

fun GDriveFileDto.toBackupDetails(): BackupDetails = BackupDetails(
    name = name.substringAfter("-"),
    id = id,
    backupDateTime = DateUtil.parse(name.substringBefore("-"))
)