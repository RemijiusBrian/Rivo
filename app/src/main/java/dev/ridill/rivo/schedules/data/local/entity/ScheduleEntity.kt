package dev.ridill.rivo.schedules.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import dev.ridill.rivo.core.data.db.RivoDatabase
import java.time.LocalDateTime

@Entity(tableName = "schedules_table")
data class ScheduleEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = RivoDatabase.DEFAULT_ID_LONG,

    @ColumnInfo(name = "amount")
    val amount: Double,

    @ColumnInfo(name = "note")
    val note: String?,

    @ColumnInfo(name = "type")
    val typeName: String,

    @ColumnInfo(name = "tag_id")
    val tagId: Long?,

    @ColumnInfo(name = "folder_id")
    val folderId: Long?,

    @ColumnInfo(name = "repeat_mode")
    val repeatModeName: String,

    @ColumnInfo(name = "next_reminder_date")
    val nextReminderDate: LocalDateTime?,

    @ColumnInfo(name = "last_paid_date")
    val lastPaidDate: LocalDateTime?
)