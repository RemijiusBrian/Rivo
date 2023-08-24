package dev.ridill.mym.expense.domain.sms

import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.telephony.SmsMessage
import com.google.mlkit.nl.entityextraction.Entity
import com.google.mlkit.nl.entityextraction.EntityExtraction
import com.google.mlkit.nl.entityextraction.EntityExtractionParams
import com.google.mlkit.nl.entityextraction.EntityExtractor
import com.google.mlkit.nl.entityextraction.EntityExtractorOptions
import dev.ridill.mym.R
import dev.ridill.mym.core.domain.util.DateUtil
import dev.ridill.mym.core.domain.util.TextFormat
import dev.ridill.mym.core.domain.util.WhiteSpace
import dev.ridill.mym.core.domain.util.orZero
import dev.ridill.mym.core.domain.util.tryOrNull
import dev.ridill.mym.expense.domain.notification.AutoAddExpenseNotificationHelper
import dev.ridill.mym.expense.domain.repository.ExpenseRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ExpenseSmsService(
    private val repo: ExpenseRepository,
    private val notificationHelper: AutoAddExpenseNotificationHelper,
    private val applicationScope: CoroutineScope,
    private val context: Context
) {
    private val merchantRegex = MERCHANT_PATTERN.toRegex()

    fun isSmsActionValid(action: String?): Boolean =
        action == Telephony.Sms.Intents.SMS_RECEIVED_ACTION

    fun saveExpenseFromSMSData(data: Intent) = applicationScope.launch(Dispatchers.Default) {
        val messages = getSmsFromIntent(data)
        getEntityExtractor().use { extractor ->
            extractor.downloadModelIfNeeded().await()
            if (!extractor.isModelDownloaded.await()) return@launch

            for (message in messages) {
                val content = message.messageBody
                if (!isDebitSms(content)) continue

                val expenseDetails = extractExpenseDetails(extractor, content)
                    ?: continue
                val amount = expenseDetails.amount
                val merchant = expenseDetails.merchant
                    ?: context.getString(R.string.generic_merchant)

                val insertedId = repo.cacheExpense(
                    id = null,
                    amount = amount,
                    note = merchant,
                    dateTime = DateUtil.now(),
                    tagId = null
                )

                notificationHelper.postNotification(
                    id = insertedId.toInt(),
                    title = context.getString(R.string.new_expense_detected),
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

    private fun isDebitSms(content: String): Boolean =
        content.contains("debited", true)
                || content.contains("spent", true)

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

    private suspend fun extractExpenseDetails(
        extractor: EntityExtractor,
        content: String
    ): ExpenseDetailsFromSMS? = tryOrNull {
        val params = EntityExtractionParams.Builder(content)
            .setEntityTypesFilter(
                setOf(Entity.TYPE_MONEY)
            )
            .build()

        val moneyEntity = extractor.annotate(params).await()
            .firstOrNull()
            ?.entities
            ?.find { it.type == Entity.TYPE_MONEY }
            ?.asMoneyEntity()
            ?: return@tryOrNull null

        val integer = moneyEntity.integerPart.orZero()
        val fraction = ("0.${moneyEntity.fractionalPart.orZero()}").toDouble()
        val amount = integer + fraction

        val merchant = extractMerchant(content)

        ExpenseDetailsFromSMS(
            amount = amount,
            merchant = merchant
        )
    }
}

private const val MERCHANT_PATTERN =
    "(?i)(?:\\sat\\s|in\\*|to\\s)([A-Za-z0-9]*\\s?-?\\s?[A-Za-z0-9]*\\s?-?\\.?)"

data class ExpenseDetailsFromSMS(
    val amount: Double,
    val merchant: String?
)