package dev.ridill.mym.expense.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import dev.ridill.mym.core.data.db.MYMDatabase
import java.time.LocalDateTime

@Entity
data class TagEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = MYMDatabase.DEFAULT_ID_LONG,
    val name: String,
    val colorCode: Int,
    val createdTimestamp: LocalDateTime
)