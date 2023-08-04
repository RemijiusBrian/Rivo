package dev.ridill.mym.expense.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity
data class TagEntity(
    @PrimaryKey(autoGenerate = false)
    val name: String,
    val colorCode: Int,
    val dateCreated: LocalDateTime
)