package dev.ridill.rivo.transactions.data.local.views

import androidx.room.DatabaseView
import java.time.LocalDateTime

@DatabaseView(
    value = """SELECT tx.id AS transactionId,
        tx.note AS transactionNote,
        tx.amount AS transactionAmount,
        tx.timestamp AS transactionTimestamp,
        tx.type AS transactionTypeName,
        tx.is_excluded AS isTransactionExcluded,
        tag.id AS tagId,
        tag.name AS tagName,
        tag.color_code AS tagColorCode,
        tag.created_timestamp AS tagCreatedTimestamp,
        tag.is_excluded AS isTagExcluded,
        folder.id AS folderId,
        folder.name AS folderName,
        folder.created_timestamp AS folderCreatedTimestamp,
        folder.is_excluded AS isFolderExcluded,
        (CASE WHEN 1 IN (tx.is_excluded, tag.is_excluded, folder.is_excluded) THEN 1 ELSE 0 END) AS overallExcluded
        FROM transaction_table tx
        LEFT OUTER JOIN tag_table tag ON tx.tag_id = tag.id
        LEFT OUTER JOIN folder_table folder ON tx.folder_id = folder.id""",
    viewName = "transaction_details_view"
)
data class TransactionDetailsView(
    val transactionId: Long,
    val transactionNote: String,
    val transactionAmount: Double,
    val transactionTimestamp: LocalDateTime,
    val transactionTypeName: String,
    val isTransactionExcluded: Boolean,
    val tagId: Long?,
    val tagName: String?,
    val tagColorCode: Int?,
    val tagCreatedTimestamp: LocalDateTime?,
    val isTagExcluded: Boolean?,
    val folderId: Long?,
    val folderName: String?,
    val folderCreatedTimestamp: LocalDateTime?,
    val isFolderExcluded: Boolean?,
    val overallExcluded: Boolean
)