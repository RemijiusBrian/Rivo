package dev.ridill.rivo.folders.domain.model

import java.time.LocalDateTime

data class TransactionFolder(
    val id: Long,
    val name: String,
    val createdTimestamp: LocalDateTime,
    val excluded: Boolean
)