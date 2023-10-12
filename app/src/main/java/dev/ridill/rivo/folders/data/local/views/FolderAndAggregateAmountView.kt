package dev.ridill.rivo.folders.data.local.views

import androidx.room.DatabaseView
import java.time.LocalDateTime

@DatabaseView(
    value = """SELECT folder.id AS id,
        folder.name AS name,
        folder.created_timestamp AS createdTimestamp,
        folder.is_excluded as excluded,
        ((SELECT IFNULL(SUM(tx1.amount), 0.0) FROM transaction_table tx1 WHERE tx1.folder_id = folder.id AND tx1.type = 'DEBIT')
        - (SELECT IFNULL(SUM(tx2.amount), 0.0) FROM transaction_table tx2 WHERE tx2.folder_id = folder.id AND tx2.type = 'CREDIT')
        ) AS aggregateAmount
        FROM folder_table folder""",
    viewName = "folder_and_aggregate_amount_view"
)
data class FolderAndAggregateAmountView(
    val id: Long,
    val name: String,
    val createdTimestamp: LocalDateTime,
    val excluded: Boolean,
    val aggregateAmount: Double
)