package dev.ridill.rivo.transactionGroups.domain.model

import java.time.LocalDateTime

data class TxGroup(
    val id: Long,
    val name: String,
    val createdTimestamp: LocalDateTime,
    val excluded: Boolean
)