package dev.ridill.mym.settings.data

import dev.ridill.mym.core.domain.util.DateUtil
import dev.ridill.mym.settings.data.remote.dto.GDriveFileDto
import dev.ridill.mym.settings.domain.modal.BackupFile

fun GDriveFileDto.toBackupFile(): BackupFile = BackupFile(
    name = name.substringAfter("-"),
    id = id,
    backupDateTime = DateUtil.parse(name.substringBefore("-")) ?: DateUtil.now()
)