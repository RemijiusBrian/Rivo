package dev.ridill.mym.expense.domain.sms

import android.content.Intent
import android.provider.Telephony
import android.telephony.SmsMessage

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

    fun extractAmount(content: String): String? =
        amountRegex.find(content)?.value

    fun extractMerchant(content: String): String? =
        merchantRegex.find(content)?.value
}

private const val SENDER_PATTERN = "[a-zA-Z0-9]{2}-[a-zA-Z0-9]{6}"
private const val AMOUNT_PATTERN =
    "(?i)(?:RS|INR|MRP\\.?\\s?)(\\d+(:?,\\d+)?(,\\d+)?(\\.\\d{1,2})?)"
private const val MERCHANT_PATTERN =
    "(?i)(?:\\sat\\s|in\\*)([A-Za-z0-9]*\\s?-?\\s?[A-Za-z0-9]*\\s?-?\\.?)"