package dev.ridill.rivo.transactions.data.local.relations

import java.time.LocalDateTime

data class TransactionDetails(
    val transactionId: Long,
    val transactionNote: String,
    val transactionAmount: Double,
    val transactionTimestamp: LocalDateTime,
    val transactionTypeName: String,
    val tagId: Long?,
    val tagName: String?,
    val tagColorCode: Int?,
    val tagCreatedTimestamp: LocalDateTime?,
    val groupId: Long?,
    val groupName: String?,
    val groupCreatedTimestamp: LocalDateTime?,
    val isExcludedTransaction: Boolean
)