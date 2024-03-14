package dev.ridill.rivo.transactionSchedules.data.local.relation

import androidx.room.Embedded
import androidx.room.Relation
import dev.ridill.rivo.transactionSchedules.data.local.entity.TxScheduleEntity
import dev.ridill.rivo.transactions.data.local.entity.TransactionEntity

data class ScheduleWithLastTransactionRelation(
    @Embedded val schedule: TxScheduleEntity,
    @Relation(
        entity = TransactionEntity::class,
        entityColumn = "schedule_id",
        parentColumn = "id"
    ) val transactionEntity: TransactionEntity?
)