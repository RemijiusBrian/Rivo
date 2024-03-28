package dev.ridill.rivo.transactions.domain.autoDetection

import dev.ridill.rivo.transactions.domain.model.TransactionType
import java.time.LocalDateTime

interface TransactionDataExtractor {
    fun isOriginValidOrg(originatingAddress: String): Boolean

    @Throws(TransactionDataExtractionFailedThrowable::class)
    fun extractData(messageBody: String): ExtractedTransactionData
}

data class ExtractedTransactionData(
    val amount: Double,
    val paymentTimestamp: LocalDateTime,
    val transactionType: TransactionType,
    val secondParty: String?
)

class TransactionDataExtractionFailedThrowable(message: String) : Throwable(message)