package dev.ridill.rivo.transactions.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import dev.ridill.rivo.core.data.db.RivoDatabase
import java.time.LocalDateTime

@Entity(tableName = "tag_table")
data class TagEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = RivoDatabase.DEFAULT_ID_LONG,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "color_code")
    val colorCode: Int,

    @ColumnInfo(name = "created_timestamp")
    val createdTimestamp: LocalDateTime,

    @ColumnInfo(name = "is_excluded")
    val isExcluded: Boolean
)