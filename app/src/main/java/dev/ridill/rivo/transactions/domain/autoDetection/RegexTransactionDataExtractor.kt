package dev.ridill.rivo.transactions.domain.autoDetection

import dev.ridill.rivo.core.domain.util.DateUtil
import dev.ridill.rivo.core.domain.util.Empty
import dev.ridill.rivo.core.domain.util.LocaleUtil
import dev.ridill.rivo.core.domain.util.WhiteSpace
import dev.ridill.rivo.core.domain.util.Zero
import dev.ridill.rivo.core.domain.util.ifNaN
import dev.ridill.rivo.transactions.domain.model.TransactionType
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * [TransactionDataExtractor] implementation using [Regex] pattern matching.
 *
 * This implementation is not my own
 * and was adopted from the [transaction-sms-parser](https://github.com/saurabhgupta050890/transaction-sms-parser) library
 * by user **saurabhgupta050890**. Checkout their GitHub page [here.](https://github.com/saurabhgupta050890)
 */

class RegexTransactionDataExtractor : TransactionDataExtractor {
    private val creditRegex = CREDIT_PATTERN.toRegex()
    private val debitRegex = DEBIT_PATTERN.toRegex()
    private val miscPaymentRegex = MISC_PAYMENT_PATTERN.toRegex()
    private val timestampRegex = TIMESTAMP_PATTERN.toRegex()

    @Throws(TransactionDataExtractionFailedThrowable::class)
    override fun extractData(messageBody: String): ExtractedTransactionData {
        val processedContent = processText(messageBody)

        val amount = getTransactionAmount(processedContent)
        val merchant = getMerchant(processedContent)
            ?: throw TransactionDataExtractionFailedThrowable()
        val transactionType = getTransactionType(processedContent)
            ?: throw TransactionDataExtractionFailedThrowable()
        val timestamp = getTimestamp(processedContent)
            ?: throw TransactionDataExtractionFailedThrowable()

        return ExtractedTransactionData(
            amount = amount,
            paymentTimestamp = timestamp,
            transactionType = transactionType,
            secondParty = merchant
        )
    }

    private fun getTransactionAmount(content: List<String>): Double {
        val index = content.indexOfFirst { it.startsWith("rs.") }
            .takeIf { it > -1 }
            ?: return Double.Zero

        val amount = content[index]
            .removePrefix("rs.")
            .toDoubleOrNull()
            ?: Double.Zero

        return amount.ifNaN { Double.Zero }
    }

    private fun getMerchant(content: List<String>): String? {
        val joinedString = content.joinToString(String.WhiteSpace)
        var merchant: String? = null
        if ("vpa" in content) {
            val index = content.indexOf("vpa")
            if (index < content.lastIndex) {
                val nextStr = content[index + 1]
                merchant = nextStr
            }
        }

        var match = ""
        upiKeywords.forEach { keyword ->
            if (joinedString.indexOf(keyword) > 0) {
                match = keyword
            }
        }

        if (match.isNotEmpty()) {
            val nextWord = getNextWord(joinedString, match)

            if (merchant.isNullOrEmpty()) merchant = nextWord
        }

        if (merchant.isNullOrEmpty()) {
            val index = content.indexOf("at")
            if (index > -1) {
                merchant = content[index + 1]
            }
            val wordAfter = content[index + 2]
            if (wordAfter != "on") {
                merchant = "$merchant $wordAfter"
            }
        }

        return merchant
            ?.trim('.', ',', ' ', '-', '_')
            ?.replaceFirstChar {
                if (it.isLowerCase()) it.titlecase(LocaleUtil.defaultLocale)
                else it.toString()
            }
    }

    private fun getTransactionType(content: List<String>): TransactionType? {
        val contentString = content.joinToString(String.WhiteSpace)

        return when (true) {
            contentString.contains(debitRegex) -> TransactionType.DEBIT
            contentString.contains(miscPaymentRegex) -> TransactionType.DEBIT
            contentString.contains(creditRegex) -> TransactionType.CREDIT
            else -> null
        }
    }

    private fun getTimestamp(content: List<String>): LocalDateTime? {
        val contentString = content.joinToString(String.WhiteSpace)
        return timestampRegex.find(contentString)
            ?.groupValues
            ?.getOrNull(1)
            ?.let {
                DateUtil.parse(it, DateTimeFormatter.ofPattern("yyyy-MM-dd:HH:mm:ss"))
            }
    }

    private fun getNextWord(source: String, searchWord: String, count: Int = 1): String {
        val splits = source.split(searchWord, ignoreCase = true, limit = 2)
        splits.getOrNull(1)?.let { nextGroup ->
            val workSplitRegex = "/[^0-9a-zA-Z]+/gi".toRegex()
            return nextGroup.trim().split(workSplitRegex, count).joinToString(String.WhiteSpace)
        }

        return String.Empty
    }

    private fun processText(string: String): List<String> {
        var message = string.lowercase(LocaleUtil.defaultLocale)
        // remove '-'
        message = message.replace("/-/g".toRegex(), String.Empty)
        // remove '!'
        message = message.replace("/!/g".toRegex(), "")
        // remove ':'
        message = message.replace("/:/g".toRegex(), " ")
        // remove '/'
        message = message.replace("//g".toRegex(), "")
        // remove '='
        message = message.replace("/=/g".toRegex(), " ")
        // remove '{}'
        message = message.replace("/[{}]/g".toRegex(), " ")
        // remove \n
        message = message.replace("/\n/g".toRegex(), " ")
        // remove \r
        message = message.replace("/\r/g".toRegex(), " ")
        // remove 'ending'
        message = message.replace("/ending/g".toRegex(), "")
        // replace 'x'
        message = message.replace("/x|[*]/g".toRegex(), "")
        // // remove 'is' 'with'
        // message = message.replace(/\bis\b|\bwith\b/g, '')
        // replace 'is'
        message = message.replace("/is/g".toRegex(), "")
        // replace 'with'
        message = message.replace("/with/g".toRegex(), "")
        // remove 'no.'
        message = message.replace("/no\\./g".toRegex(), "")
        // replace all ac, acct, account with ac
        message = message.replace("/\bac\b|\bacct\b|\baccount\b/g".toRegex(), "ac")
        // replace all 'rs' with 'rs. '
        message = message.replace("/rs(?=\\w)/g".toRegex(), "rs. ")
        // replace all 'rs ' with 'rs. '
        message = message.replace("/rs /g".toRegex(), "rs. ")
        // replace all inr with rs.
        message = message.replace("/inr(?=\\w)/g".toRegex(), "rs. ")
        //
        message = message.replace("/inr /g".toRegex(), "rs. ")
        // replace all 'rs. ' with 'rs.'
        message = message.replace("/rs\\. /g".toRegex(), "rs.")
        // replace all 'rs.' with 'rs. '
        message = message.replace("/rs\\.(?=\\w)/g".toRegex(), "rs. ")
        // replace all 'debited' with ' debited '
        message = message.replace("/debited/g".toRegex(), " debited ")
        // replace all 'credited' with ' credited '
        message = message.replace("/credited/g".toRegex(), " credited ")
        // combine words
        /*combinedWords.forEach((word) => {
            message = message.replace(word.regex, word.word);
        })*/
        return message.split(" ").filter { it.isNotEmpty() }
    }

    companion object {
        val upiKeywords: List<String>
            get() = listOf("upi", "ref no", "upi ref", "upi ref no")
    }
}

private const val CREDIT_PATTERN =
    "(?i)(?:credited|credit|deposited|added|received|refund|repayment)"
private const val DEBIT_PATTERN = "(?i)(?:debited|debit|deducted)"
private const val MISC_PAYMENT_PATTERN =
    "(?i)(?:payment|spent|paid|used\\s+at|charged|transaction\\son|transaction\\sfee|tran|booked|purchased|sent\\s+to|purchase\\s+of)"
private const val TIMESTAMP_PATTERN = "(?i)on\\s+(\\d{4}-\\d{2}-\\d{2}:\\d{2}:\\d{2}:\\d{2})"