package dev.ridill.mym.settings.domain.modal

import java.io.File

data class DBCache(
    val dbFile: File,
    val walFile: File?,
    val shmFile: File?
)