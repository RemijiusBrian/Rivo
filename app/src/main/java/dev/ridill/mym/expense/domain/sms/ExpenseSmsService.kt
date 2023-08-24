package dev.ridill.mym.expense.domain.sms

import android.content.Intent
import android.provider.Telephony
import android.telephony.SmsMessage
import com.google.mlkit.nl.entityextraction.Entity
import com.google.mlkit.nl.entityextraction.EntityExtraction
import com.google.mlkit.nl.entityextraction.EntityExtractionParams
import com.google.mlkit.nl.entityextraction.EntityExtractor
import com.google.mlkit.nl.entityextraction.EntityExtractorOptions
import dev.ridill.mym.core.domain.util.WhiteSpace
import dev.ridill.mym.core.domain.util.orZero
import dev.ridill.mym.core.domain.util.tryOrNull
import kotlinx.coroutines.tasks.await

class ExpenseSmsService {
    private val merchantRegex = MERCHANT_PATTERN.toRegex()

    fun isSmsActionValid(action: String?): Boolean =
        action == Telephony.Sms.Intents.SMS_RECEIVED_ACTION

    fun getSmsFromIntent(intent: Intent): List<SmsMessage> =
        Telephony.Sms.Intents.getMessagesFromIntent(intent).toList()

    fun isDebitSms(content: String): Boolean =
        content.contains("debited", true)
                || content.contains("spent", true)

    private fun extractMerchant(content: String): String? {
        val groupValues = merchantRegex.find(content)?.groupValues ?: return null

        return groupValues
            .subList(1, groupValues.size)
            .joinToString(String.WhiteSpace)
    }

    suspend fun extractExpenseDetails(
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

    fun getEntityExtractor(): EntityExtractor = EntityExtraction.getClient(
        EntityExtractorOptions.Builder(EntityExtractorOptions.ENGLISH)
            .build()
    )
}

private const val MERCHANT_PATTERN =
    "(?i)(?:\\sat\\s|in\\*|to\\s)([A-Za-z0-9]*\\s?-?\\s?[A-Za-z0-9]*\\s?-?\\.?)"

data class ExpenseDetailsFromSMS(
    val amount: Double,
    val merchant: String?
)