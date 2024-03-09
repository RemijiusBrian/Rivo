package dev.ridill.rivo.transactions.domain.sms

import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.telephony.SmsMessage
import com.google.mlkit.nl.entityextraction.Entity
import com.google.mlkit.nl.entityextraction.EntityExtraction
import com.google.mlkit.nl.entityextraction.EntityExtractionParams
import com.google.mlkit.nl.entityextraction.EntityExtractor
import com.google.mlkit.nl.entityextraction.EntityExtractorOptions
import dev.ridill.rivo.R
import dev.ridill.rivo.core.data.db.RivoDatabase
import dev.ridill.rivo.core.domain.notification.NotificationHelper
import dev.ridill.rivo.core.domain.util.DateUtil
import dev.ridill.rivo.core.domain.util.WhiteSpace
import dev.ridill.rivo.core.domain.util.logD
import dev.ridill.rivo.core.domain.util.orZero
import dev.ridill.rivo.core.domain.util.tryOrNull
import dev.ridill.rivo.core.ui.util.TextFormat
import dev.ridill.rivo.transactions.domain.model.Transaction
import dev.ridill.rivo.transactions.domain.model.TransactionType
import dev.ridill.rivo.transactions.domain.repository.AddEditTransactionRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.time.LocalDateTime
import java.util.Locale

class TransactionSmsService(
    private val repo: AddEditTransactionRepository,
    private val notificationHelper: NotificationHelper<Transaction>,
    private val applicationScope: CoroutineScope,
    private val context: Context
) {
    private val debitReceiverRegex = DEBIT_RECEIVER_PATTERN.toRegex()
    private val creditSenderRegex = CREDIT_SENDER_PATTERN.toRegex()
    private val creditTextRegex = CREDIT_TEXT_PATTERN.toRegex()
    private val debitTextPattern = DEBIT_TEXT_PATTERN.toRegex()

    suspend fun downloadModelIfNeeded() {
        getEntityExtractor().use {
            it.downloadModelIfNeeded().await()
        }
    }

    fun isSmsActionValid(action: String?): Boolean =
        action == Telephony.Sms.Intents.SMS_RECEIVED_ACTION

    fun saveTransactionsFromSMSData(data: Intent) = applicationScope.launch(Dispatchers.Default) {
        getEntityExtractor().use { extractor ->
            extractor.downloadModelIfNeeded().await()
            if (!extractor.isModelDownloaded.await()) return@launch

            val dateTimeNow = DateUtil.now()
            val currency = repo.getCurrencyPreference(dateTimeNow).first()
            val messages = getSmsFromIntent(data)
            for (message in messages) {
                val content = message.messageBody

                val transactionDetails = extractTransactionDetails(extractor, content)
                    ?: continue
                logD { "Transaction Details - $transactionDetails" }
                val timestamp = transactionDetails.paymentTimestamp
                if (timestamp.isAfter(dateTimeNow)) continue

                val amount = transactionDetails.amount
                val type = transactionDetails.type
                val secondParty = transactionDetails.secondParty
                    ?: context.getString(
                        when (type) {
                            TransactionType.CREDIT -> R.string.generic_credit_sender
                            TransactionType.DEBIT -> R.string.generic_debit_receiver
                        }
                    )
                val note = context.getString(
                    when (type) {
                        TransactionType.CREDIT -> R.string.received_from_sender
                        TransactionType.DEBIT -> R.string.spent_towards_receiver
                    },
                    secondParty
                )

                val transaction = Transaction(
                    id = RivoDatabase.DEFAULT_ID_LONG,
                    amount = amount.toString(),
                    note = note,
                    timestamp = timestamp,
                    type = type,
                    excluded = false,
                    tagId = null,
                    folderId = null
                )

                val insertedId = repo.saveTransaction(transaction)

                notificationHelper.postNotification(
                    id = insertedId.hashCode(),
                    data = transaction.copy(
                        id = insertedId,
                        amount = TextFormat.currency(amount, currency)
                    )
                )
            }
        }
    }

    private fun getSmsFromIntent(intent: Intent): List<SmsMessage> =
        Telephony.Sms.Intents.getMessagesFromIntent(intent).toList()

    private fun extractSecondParty(content: String, type: TransactionType): String? =
        when (type) {
            TransactionType.CREDIT -> creditSenderRegex.find(content)?.groupValues
            TransactionType.DEBIT -> debitReceiverRegex.find(content)?.groupValues
        }?.let {
            it.subList(1, it.size)
                .joinToString(String.WhiteSpace)
        }

    private fun getEntityExtractor(): EntityExtractor = EntityExtraction.getClient(
        EntityExtractorOptions.Builder(EntityExtractorOptions.ENGLISH)
            .build()
    )

    private suspend fun extractTransactionDetails(
        extractor: EntityExtractor,
        content: String
    ): TransactionDetailsFromSMS? = tryOrNull {
        val params = EntityExtractionParams.Builder(content)
            .setEntityTypesFilter(
                setOf(Entity.TYPE_MONEY, Entity.TYPE_DATE_TIME)
            )
            .setPreferredLocale(Locale.getDefault())
            .build()

        val annotations = extractor.annotate(params).await()
            .ifEmpty { throw AnnotationFailedThrowable() }

        logD { "Annotations - $annotations" }

        val moneyEntity = annotations
            ?.find { annotation -> annotation.entities.any { it.type == Entity.TYPE_MONEY } }
            ?.entities
            ?.firstOrNull()
            ?.asMoneyEntity()
            ?: throw MoneyExtractionFailedThrowable()

        val integer = moneyEntity.integerPart.orZero()
        val fraction =
            moneyEntity.fractionalPart.orZero() * 0.1 // Convert whole Int fractionPart to double 0.{fractionPart}
        val amount = integer + fraction
        logD { "Amount - $amount" }

        val paymentTimestamp = annotations
            .find { annotation -> annotation.entities.any { it.type == Entity.TYPE_DATE_TIME } }
            ?.entities
            ?.firstOrNull()
            ?.asDateTimeEntity()
            ?.timestampMillis
            ?.let { DateUtil.fromMillis(it) }
            ?: throw DateExtractionFailedThrowable()
        logD { "Payment Timestamp - $paymentTimestamp" }

        val transactionType = extractTransactionType(content)
            ?: throw InvalidTransactionTypeThrowable()
        logD { "Transaction Type - $transactionType" }
        val secondParty = extractSecondParty(content, transactionType)
        logD { "Second Party - $secondParty" }

        TransactionDetailsFromSMS(
            amount = amount,
            secondParty = secondParty,
            paymentTimestamp = paymentTimestamp,
            type = transactionType
        )
    }

    private fun extractTransactionType(content: String): TransactionType? = when (true) {
        (content.contains(creditTextRegex)
                && !content.contains(debitTextPattern)) -> TransactionType.CREDIT

        (content.contains(debitTextPattern)) -> TransactionType.DEBIT

        else -> null
    }
}

private const val DEBIT_RECEIVER_PATTERN =
    "(?i)(?:\\sat\\s|in\\*|to\\s|to\\sVPA\\s)([A-Za-z0-9]*\\s?-?\\s?[A-Za-z0-9]*\\s?-?\\.?[A-Za-z0-9]*)"
private const val CREDIT_SENDER_PATTERN =
    "(?i)(?:\\sby\\*|\\slinked\\sto\\sVPA\\s)([A-Za-z0-9]*\\s?-?\\s?[A-Za-z0-9]*\\s?-?\\.?[A-Za-z0-9]*)"
private const val CREDIT_TEXT_PATTERN = "(?i)(credit(ed)?|receive(d)?)"
private const val DEBIT_TEXT_PATTERN = "(?i)(debit(ed)|spent|sent)?"

data class TransactionDetailsFromSMS(
    val amount: Double,
    val paymentTimestamp: LocalDateTime,
    val type: TransactionType,
    val secondParty: String?
)

class AnnotationFailedThrowable : Throwable("Failed to annotate data")
class MoneyExtractionFailedThrowable : Throwable("Failed to extract money entity")
class DateExtractionFailedThrowable : Throwable("Failed to extract date entity")
class InvalidTransactionTypeThrowable : Throwable("Unable to identify transaction type")