package dev.ridill.rivo.transactionSchedules.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import dev.ridill.rivo.core.data.db.RivoDatabase
import java.time.LocalDate

@Entity(tableName = "transaction_schedules_table")
data class TxScheduleEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = RivoDatabase.DEFAULT_ID_LONG,

    @ColumnInfo(name = "amount")
    val amount: Double,

    @ColumnInfo(name = "note")
    val note: String?,

    @ColumnInfo(name = "type")
    val typeName: String,

    @ColumnInfo(name = "repeat_mode")
    val repeatModeName: String,

    @ColumnInfo(name = "next_reminder_date")
    val nextReminderDate: LocalDate?
)