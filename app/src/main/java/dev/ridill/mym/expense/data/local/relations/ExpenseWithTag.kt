package dev.ridill.mym.expense.data.local.relations

import androidx.room.Embedded
import androidx.room.Relation
import dev.ridill.mym.expense.data.local.entity.ExpenseEntity
import dev.ridill.mym.expense.data.local.entity.TagEntity

data class ExpenseWithTag(
    @Embedded
    val expenseEntity: ExpenseEntity,
    @Relation(
        entity = TagEntity::class,
        parentColumn = "tagId",
        entityColumn = "name"
    ) val tagEntity: TagEntity?
)