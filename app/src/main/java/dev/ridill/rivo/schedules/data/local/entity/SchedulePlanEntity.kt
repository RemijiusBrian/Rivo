package dev.ridill.rivo.schedules.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "schedule_plan_table")
data class SchedulePlanEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "color_code")
    val colorCode: Int,

    @ColumnInfo(name = "created_timestamp")
    val createdTimestamp: LocalDateTime
)