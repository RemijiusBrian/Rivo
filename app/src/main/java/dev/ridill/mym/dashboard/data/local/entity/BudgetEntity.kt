package dev.ridill.mym.dashboard.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity
data class BudgetEntity(
    @PrimaryKey(autoGenerate = false)
    val amount: Long,
    val createdTimestamp: LocalDateTime,
    val isCurrent: Boolean
)