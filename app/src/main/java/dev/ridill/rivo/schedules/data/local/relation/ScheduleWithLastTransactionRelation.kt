package dev.ridill.rivo.schedules.data.local.relation

import androidx.room.Embedded
import androidx.room.Relation
import dev.ridill.rivo.schedules.data.local.entity.ScheduleEntity
import dev.ridill.rivo.transactions.data.local.entity.TransactionEntity

data class ScheduleWithLastTransactionRelation(
    @Embedded val schedule: ScheduleEntity,
    @Relation(
        entity = TransactionEntity::class,
        entityColumn = "schedule_id",
        parentColumn = "id"
    ) val transactionEntity: TransactionEntity?
)