package dev.ridill.rivo.transactionGroups.data.local.relation

import java.time.LocalDateTime

data class GroupAndAggregateAmount(
    val id: Long,
    val name: String,
    val createdTimestamp: LocalDateTime,
    val aggregateAmount: Double
)