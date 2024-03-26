package dev.ridill.rivo.transactions.domain.autoDetection

import dev.ridill.rivo.transactions.domain.model.TransactionType
import java.time.LocalDateTime

interface TransactionDataExtractor {

    @Throws(TransactionDataExtractionFailedThrowable::class)
    fun extractData(messageBody: String): ExtractedTransactionData
}

data class ExtractedTransactionData(
    val amount: Double,
    val paymentTimestamp: LocalDateTime,
    val transactionType: TransactionType,
    val secondParty: String?
)

class TransactionDataExtractionFailedThrowable : Throwable("Failed to extract transaction data")