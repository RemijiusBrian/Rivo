package dev.ridill.mym.expense.domain.sms

import android.content.Intent
import android.provider.Telephony
import android.telephony.SmsMessage
import dev.ridill.mym.core.domain.util.WhiteSpace

class ExpenseSmsService {
    private val senderRegex = SENDER_PATTERN.toRegex()
    private val amountRegex = AMOUNT_PATTERN.toRegex()
    private val merchantRegex = MERCHANT_PATTERN.toRegex()

    fun isSmsActionValid(action: String?): Boolean =
        action == Telephony.Sms.Intents.SMS_RECEIVED_ACTION

    fun getSmsFromIntent(intent: Intent): List<SmsMessage> =
        Telephony.Sms.Intents.getMessagesFromIntent(intent).toList()

    fun isBankSms(sender: String?): Boolean =
        senderRegex.matches(sender.orEmpty())

    fun isDebitSms(content: String): Boolean =
        content.contains("debited", true)
                || content.contains("spent", true)

    fun extractAmount(content: String): String? =
        amountRegex.find(content)?.groupValues?.get(1)

    fun extractMerchant(content: String): String? {
        val groupValues = merchantRegex.find(content)?.groupValues ?: return null

        return groupValues
            .subList(1, groupValues.size)
            .joinToString(String.WhiteSpace)
    }
}

private const val SENDER_PATTERN = "[a-zA-Z0-9]{2}-[a-zA-Z0-9]{6}"
private const val AMOUNT_PATTERN =
    "(?i)(?:(?:RS|INR|MRP)\\.?\\s?)(\\d+(:?\\,\\d+)?(\\,\\d+)?(\\.\\d{1,2})?)"
private const val MERCHANT_PATTERN =
    "(?i)(?:\\sat\\s|in\\*)([A-Za-z0-9]*\\s?-?\\s?[A-Za-z0-9]*\\s?-?\\.?)"