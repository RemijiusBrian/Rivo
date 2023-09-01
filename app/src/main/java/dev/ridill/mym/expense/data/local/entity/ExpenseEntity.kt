package dev.ridill.mym.expense.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import dev.ridill.mym.core.data.db.MYMDatabase
import java.time.LocalDateTime

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = TagEntity::class,
            parentColumns = ["id"],
            childColumns = ["tagId"]
        )
    ],
    indices = [Index("tagId")]
)
data class ExpenseEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = MYMDatabase.DEFAULT_ID_LONG,
    val note: String,
    val amount: Double,
    val timestamp: LocalDateTime,
    val tagId: Long?
)