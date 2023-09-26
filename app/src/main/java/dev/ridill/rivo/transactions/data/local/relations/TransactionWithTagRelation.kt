package dev.ridill.rivo.transactions.data.local.relations

import java.time.LocalDateTime

data class TransactionWithTagRelation(
    val transactionId: Long,
    val transactionNote: String,
    val transactionAmount: Double,
    val transactionTimestamp: LocalDateTime,
    val tagId: Long?,
    val tagName: String?,
    val tagColorCode: Int?,
    val tagCreatedTimestamp: LocalDateTime?,
    val isExcludedTransaction: Boolean
)