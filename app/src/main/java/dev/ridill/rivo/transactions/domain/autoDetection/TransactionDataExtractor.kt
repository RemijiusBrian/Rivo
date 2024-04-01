package dev.ridill.rivo.transactions.domain.autoDetection

import dev.ridill.rivo.transactions.domain.model.TransactionType
import java.time.LocalDateTime

interface TransactionDataExtractor {
    fun isNotSupportedLanguage(message: String): Boolean
    fun isOriginValidOrg(originatingAddress: String): Boolean

    @Throws(TransactionDataExtractionFailedThrowable::class)
    fun extractData(messageBody: String): ExtractedTransactionData
}

data class ExtractedTransactionData(
    val amount: Double,
    val paymentTimestamp: LocalDateTime,
    val transactionType: TransactionType,
    val note: String?
)

class TransactionDataExtractionFailedThrowable(message: String) : Throwable(message)