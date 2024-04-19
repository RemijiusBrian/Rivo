package dev.ridill.rivo.settings.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "currency_table")
data class CurrencyEntity(
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "currency_code")
    val currencyCode: String,

    @ColumnInfo(name = "display_name")
    val displayName: String
)