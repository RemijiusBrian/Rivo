package dev.ridill.rivo.folders.data.local.relation

import java.time.LocalDateTime

data class FolderAndAggregateAmount(
    val id: Long,
    val name: String,
    val createdTimestamp: LocalDateTime,
    val excluded: Boolean,
    val aggregateAmount: Double
)