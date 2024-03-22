package dev.ridill.rivo.schedules.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import dev.ridill.rivo.core.data.db.RivoDatabase
import java.time.LocalDate

@Entity(
    tableName = "schedules_table",
    foreignKeys = [
        ForeignKey(
            entity = SchedulePlanEntity::class,
            parentColumns = ["id"],
            childColumns = ["plan_id"]
        )
    ],
    indices = [Index("plan_id")]
)
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
    val nextReminderDate: LocalDate?,

    @ColumnInfo(name = "plan_id")
    val planId: Long?
)