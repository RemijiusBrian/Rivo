package dev.ridill.rivo.transactions.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import dev.ridill.rivo.core.data.db.RivoDatabase
import dev.ridill.rivo.folders.data.local.entity.FolderEntity
import java.time.LocalDateTime

@Entity(
    tableName = "transaction_table",
    foreignKeys = [
        ForeignKey(
            entity = TagEntity::class,
            parentColumns = ["id"],
            childColumns = ["tag_id"]
        ),
        ForeignKey(
            entity = FolderEntity::class,
            parentColumns = ["id"],
            childColumns = ["folder_id"]
        )
    ],
    indices = [Index("tag_id"), Index("folder_id"), Index("timestamp")]
)
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = RivoDatabase.DEFAULT_ID_LONG,

    @ColumnInfo(name = "note")
    val note: String,

    @ColumnInfo(name = "amount")
    val amount: Double,

    @ColumnInfo(name = "timestamp")
    val timestamp: LocalDateTime,

    @ColumnInfo(name = "type", defaultValue = "DEBIT")
    val typeName: String,

    @ColumnInfo(name = "is_excluded")
    val isExcluded: Boolean,

    @ColumnInfo(name = "tag_id")
    val tagId: Long?,

    @ColumnInfo(name = "folder_id")
    val folderId: Long?
)