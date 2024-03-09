package dev.ridill.rivo.scheduledTransaction.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import dev.ridill.rivo.core.data.db.RivoDatabase
import java.time.LocalDate

@Entity(tableName = "scheduled_transaction_table")
data class ScheduledTransactionEntity(
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

    @ColumnInfo(name = "next_payment_date")
    val nextPaymentDate: LocalDate
)