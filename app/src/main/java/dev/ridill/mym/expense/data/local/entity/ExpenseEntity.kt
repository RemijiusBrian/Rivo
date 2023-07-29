package dev.ridill.mym.expense.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import dev.ridill.mym.core.domain.util.Zero
import java.time.LocalDateTime

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = TagEntity::class,
            parentColumns = ["name"],
            childColumns = ["tagId"]
        )
    ],
    indices = [Index("tagId")]
)
data class ExpenseEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = Long.Zero,
    val note: String,
    val amount: Double,
    val dateTime: LocalDateTime,
    val tagId: String? = null
)