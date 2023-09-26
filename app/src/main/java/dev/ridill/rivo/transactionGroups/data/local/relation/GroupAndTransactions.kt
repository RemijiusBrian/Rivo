package dev.ridill.rivo.transactionGroups.data.local.relation

import androidx.room.Embedded
import androidx.room.Relation
import dev.ridill.rivo.expense.data.local.entity.TransactionEntity
import dev.ridill.rivo.transactionGroups.data.local.entity.TransactionGroupEntity

data class GroupAndTransactions(
    @Embedded val groupEntity: TransactionGroupEntity,
    @Relation(
        entity = TransactionEntity::class,
        parentColumn = "id",
        entityColumn = "group_id"
    ) val transactionEntities: List<TransactionEntity>
)