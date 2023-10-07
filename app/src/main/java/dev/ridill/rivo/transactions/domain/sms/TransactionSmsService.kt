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
import dev.ridill.rivo.core.domain.util.DateUtil
import dev.ridill.rivo.core.domain.util.WhiteSpace
import dev.ridill.rivo.core.domain.util.orZero
import dev.ridill.rivo.core.domain.util.tryOrNull
import dev.ridill.rivo.core.ui.util.TextFormat
import dev.ridill.rivo.transactions.domain.model.TransactionType
import dev.ridill.rivo.transactions.domain.notification.AutoAddTransactionNotificationHelper
import dev.ridill.rivo.transactions.domain.repository.AddEditTransactionRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.time.LocalDateTime

class TransactionSmsService(
    private val repo: AddEditTransactionRepository,
    private val notificationHelper: AutoAddTransactionNotificationHelper,
    private val applicationScope: CoroutineScope,
    private val context: Context,
) {
    private val debitReceiverRegex = DEBIT_RECEIVER_PATTERN.toRegex()
    private val creditSenderRegex = CREDIT_SENDER_PATTERN.toRegex()
    private val creditTextRegex = CREDIT_TEXT_PATTERN.toRegex()

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
            val messages = getSmsFromIntent(data)
            for (message in messages) {
                val content = message.messageBody

                val transactionDetails = extractTransactionDetails(extractor, content)
                    ?: continue
                val transactionDate = transactionDetails.paymentTimestamp
                if (transactionDate.isAfter(dateTimeNow)) continue

                val amount = transactionDetails.amount
                val type = transactionDetails.type
                val merchant = transactionDetails.secondParty
                    ?: context.getString(
                        when (type) {
                            TransactionType.CREDIT -> R.string.generic_credit_sender
                            TransactionType.DEBIT -> R.string.generic_debit_receiver
                        }
                    )

                val insertedId = repo.saveTransaction(
                    id = null,
                    amount = amount,
                    note = merchant,
                    timestamp = transactionDate,
                    tagId = null,
                    excluded = false, // Transaction added as Included in Expenditure by default when detected from SMS
                    transactionType = type,
                    folderId = null
                )

                notificationHelper.postNotification(
                    id = insertedId.toInt(),
                    title = context.getString(R.string.new_transaction_detected),
                    content = context.getString(
                        when (type) {
                            TransactionType.CREDIT -> R.string.amount_credited_notification_message
                            TransactionType.DEBIT -> R.string.amount_debited_notification_message
                        },
                        TextFormat.currency(amount),
                        merchant
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
            .build()

        val annotations = extractor.annotate(params).await()
            .ifEmpty { throw AnnotationFailedThrowable() }

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

        val paymentDateTime = annotations
            .find { annotation -> annotation.entities.any { it.type == Entity.TYPE_DATE_TIME } }
            ?.entities
            ?.firstOrNull()
            ?.asDateTimeEntity()
            ?.timestampMillis
            ?.let { DateUtil.fromMillis(it) }
            ?: throw DateExtractionFailedThrowable()

        val transactionType = getTransactionType(content)
        val secondParty = extractSecondParty(content, transactionType)

        TransactionDetailsFromSMS(
            amount = amount,
            secondParty = secondParty,
            paymentTimestamp = paymentDateTime,
            type = transactionType
        )
    }

    private fun getTransactionType(content: String): TransactionType =
        if (content.contains(creditTextRegex)) TransactionType.CREDIT
        else TransactionType.DEBIT
}

private const val DEBIT_RECEIVER_PATTERN =
    "(?i)(?:\\sat\\s|in\\*|to\\s|to\\sVPA\\s)([A-Za-z0-9]*\\s?-?\\s?[A-Za-z0-9]*\\s?-?\\.?[A-Za-z0-9]*)"
private const val CREDIT_SENDER_PATTERN =
    "(?i)(?:\\sby\\*|\\slinked\\sto\\sVPA\\s)([A-Za-z0-9]*\\s?-?\\s?[A-Za-z0-9]*\\s?-?\\.?[A-Za-z0-9]*)"
private const val CREDIT_TEXT_PATTERN = "(credit(ed)?|receive(d)?)"

data class TransactionDetailsFromSMS(
    val amount: Double,
    val secondParty: String?,
    val paymentTimestamp: LocalDateTime,
    val type: TransactionType
)

class AnnotationFailedThrowable : Throwable("Failed to annotate data")
class MoneyExtractionFailedThrowable : Throwable("Failed to extract money amount")
class DateExtractionFailedThrowable : Throwable("Failed to extract date")