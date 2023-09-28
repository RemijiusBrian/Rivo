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
    private val context: Context
) {
    private val merchantRegex = MERCHANT_PATTERN.toRegex()

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
                if (!isExpenseSMS(content)) continue

                val transactionDetails = extractTransactionDetails(extractor, content)
                    ?: continue
                if (transactionDetails.paymentDateTime.isAfter(dateTimeNow)) continue

                val amount = transactionDetails.amount
                val merchant = transactionDetails.merchant
                    ?: context.getString(R.string.generic_merchant)

                val insertedId = repo.saveTransaction(
                    id = null,
                    amount = amount,
                    note = merchant,
                    dateTime = DateUtil.now(),
                    tagId = null,
                    excluded = false, // Transaction added as Included in Expenditure by default when detected from SMS
                    transactionType = TransactionType.DEBIT,
                    folderId = null
                )

                notificationHelper.postNotification(
                    id = insertedId.toInt(),
                    title = context.getString(R.string.new_transaction_detected),
                    content = context.getString(
                        R.string.amount_spent_towards_merchant,
                        TextFormat.currency(amount),
                        merchant
                    )
                )
            }
        }
    }

    private fun getSmsFromIntent(intent: Intent): List<SmsMessage> =
        Telephony.Sms.Intents.getMessagesFromIntent(intent).toList()

    private fun isExpenseSMS(content: String): Boolean =
        content.contains("debited", true)
                || content.contains("spent", true)
                || content.contains("money transfer", true)

    private fun extractMerchant(content: String): String? {
        val groupValues = merchantRegex.find(content)?.groupValues ?: return null

        return groupValues
            .subList(1, groupValues.size)
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

        val entities = extractor.annotate(params).await()
            .firstOrNull()
            ?.entities
        val moneyEntity = entities
            ?.find { it.type == Entity.TYPE_MONEY }
            ?.asMoneyEntity()
            ?: throw MoneyExtractionFailedThrowable()

        val integer = moneyEntity.integerPart.orZero()
        val fraction = ("0.${moneyEntity.fractionalPart.orZero()}").toDouble()
        val amount = integer + fraction

        val paymentDateTime = entities
            .find { it.type == Entity.TYPE_DATE_TIME }
            ?.asDateTimeEntity()
            ?.timestampMillis
            ?.let { DateUtil.fromMillis(it) }
            ?: throw DateExtractionFailedThrowable()

        val merchant = extractMerchant(content)

        TransactionDetailsFromSMS(
            amount = amount,
            merchant = merchant,
            paymentDateTime = paymentDateTime
        )
    }
}

private const val MERCHANT_PATTERN =
    "(?i)(?:\\sat\\s|in\\*|to\\s)([A-Za-z0-9]*\\s?-?\\s?[A-Za-z0-9]*\\s?-?\\.?)"

data class TransactionDetailsFromSMS(
    val amount: Double,
    val merchant: String?,
    val paymentDateTime: LocalDateTime,
)

class MoneyExtractionFailedThrowable : Throwable("Failed to extract money amount")
class DateExtractionFailedThrowable : Throwable("Failed to extract date")