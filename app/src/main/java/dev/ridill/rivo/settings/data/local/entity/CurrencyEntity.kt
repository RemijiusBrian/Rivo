package dev.ridill.rivo.settings.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "currency_table")
data class CurrencyEntity(
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "date")
    val date: LocalDate,

    @ColumnInfo(name = "currency_code")
    val currencyCode: String
)