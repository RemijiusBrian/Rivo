package dev.ridill.rivo.transactions.domain.autoDetection

import dev.ridill.rivo.core.domain.util.DateUtil
import dev.ridill.rivo.core.domain.util.Empty
import dev.ridill.rivo.core.domain.util.LocaleUtil
import dev.ridill.rivo.core.domain.util.WhiteSpace
import dev.ridill.rivo.core.domain.util.Zero
import dev.ridill.rivo.core.domain.util.ifNaN
import dev.ridill.rivo.core.domain.util.joinToCapitalizedString
import dev.ridill.rivo.core.domain.util.logD
import dev.ridill.rivo.core.domain.util.logI
import dev.ridill.rivo.transactions.domain.model.TransactionType
import java.time.LocalDateTime

/**
 * [TransactionDataExtractor] implementation using [Regex] pattern matching.
 *
 * This implementation is not my own
 * and was adopted from the [transaction-sms-parser](https://github.com/saurabhgupta050890/transaction-sms-parser) library
 * by user **saurabhgupta050890**. Checkout their GitHub page [here.](https://github.com/saurabhgupta050890)
 */

class RegexTransactionDataExtractor : TransactionDataExtractor {
    private val englishCharsRegex = ENGLISH_CHARS_PATTERN.toRegex()
    private val orgAddressRegex = ORG_ADDRESS_PATTERN.toRegex()
    private val creditRegex = CREDIT_PATTERN.toRegex()
    private val debitRegex = DEBIT_PATTERN.toRegex()
    private val miscPaymentRegex = MISC_PAYMENT_PATTERN.toRegex()
    private val timestampRegex = TIMESTAMP_PATTERN.toRegex()

    override fun isNotSupportedLanguage(message: String): Boolean =
        !englishCharsRegex.matches(message)

    override fun isOriginValidOrg(originatingAddress: String): Boolean =
        orgAddressRegex.matches(originatingAddress)

    @Throws(TransactionDataExtractionFailedThrowable::class)
    override fun extractData(messageBody: String): ExtractedTransactionData {
        val processedContent = processText(messageBody)

        val amount = extractTransactionAmount(processedContent)
        logD { "Extracted amount - $amount" }
        println("Extracted amount - $amount")
        val transactionType = extractTransactionType(processedContent)
        logD { "Extracted type - $transactionType" }
        println("Extracted type - $transactionType")
        val secondParty = buildNote(processedContent, transactionType)
        logD { "Transaction note - $secondParty" }
        println("note - $secondParty")
        val timestamp = extractTimestamp(messageBody)
        logD { "Extracted timestamp - $timestamp" }
        println("Extracted timestamp - $timestamp")

        return ExtractedTransactionData(
            amount = amount,
            paymentTimestamp = timestamp,
            transactionType = transactionType,
            note = secondParty
        )
    }

    @Throws(TransactionDataExtractionFailedThrowable::class)
    private fun extractTransactionAmount(content: List<String>): Double {
        println("Content for amount extraction - $content")
        val index = content.indexOfFirst { it.startsWith("rs.") }
            .takeIf { it > -1 }
            ?: throw TransactionDataExtractionFailedThrowable("Failed to find 'rs' keyword in message content: '$content'")

        val amountString = content[index + 1]
        val amount = amountString
            .removePrefix("rs.")
            .replace(",", String.Empty)
            .toDoubleOrNull()
            ?: throw TransactionDataExtractionFailedThrowable("Failed parsing amount string '$amountString' to double")

        return amount.ifNaN { Double.Zero }
    }

    @Throws(TransactionDataExtractionFailedThrowable::class)
    private fun buildNote(content: List<String>, type: TransactionType): String {
        val joinedString = content.joinToString(String.WhiteSpace)
        println("Second party joined string - $joinedString")
        var secondParty: String? = null
        if ("vpa" in content) {
            println("Content contains vpa")
            val index = content.indexOf("vpa")
            if (index < content.lastIndex) {
                val nextStr = content[index + 1]
                secondParty = nextStr
                println("Got vpa of second party - $secondParty")
            }
        }

        /*var match = ""
        upiKeywords.forEach { keyword ->
            if (joinedString.indexOf(keyword) > 0) {
                match = keyword
            }
        }

        if (match.isNotEmpty()) {
            val nextWord = getNextWord(joinedString, match)
            println("Next work from vpa - $nextWord")

            if (secondParty.isNullOrEmpty()) secondParty = nextWord
        }*/

        if (secondParty.isNullOrEmpty()) {
            var secondPartyStartIndex = content.indexOf("at")
            if (secondPartyStartIndex <= -1) {
                secondPartyStartIndex = content.indexOf("to")
                println("Found index of 'to' - $secondPartyStartIndex")
            }
            if (secondPartyStartIndex <= -1)
                throw TransactionDataExtractionFailedThrowable("Failed to find start index of second party keyword in content: '$content'")

            val secondPartyEndIndex = content
                .subList(secondPartyStartIndex + 1, content.size)
                .also { println("sublist to find end index - $it") }
                .indexOf("on")
                .also { println("Second party end index - $it") }
                .takeIf { it > -1 }
                ?: throw TransactionDataExtractionFailedThrowable("Failed to find ending index of second party in content: '$content'")

            secondParty = content
                .subList(secondPartyStartIndex + 1, secondPartyStartIndex + secondPartyEndIndex + 1)
                .also { println("Second party sublist - $it") }
                .joinToCapitalizedString()
        }

        return buildString {
            when (type) {
                TransactionType.CREDIT -> append("From")
                TransactionType.DEBIT -> append("Towards")
            }

            append(String.WhiteSpace)

            append(secondParty.trim('.', ',', ' ', '-', '_'))
        }
    }

    @Throws(TransactionDataExtractionFailedThrowable::class)
    private fun extractTransactionType(content: List<String>): TransactionType {
        val contentString = content.joinToString(String.WhiteSpace)

        return when (true) {
            contentString.contains(debitRegex) -> TransactionType.DEBIT
            contentString.contains(miscPaymentRegex) -> TransactionType.DEBIT
            contentString.contains(creditRegex) -> TransactionType.CREDIT
            else -> throw TransactionDataExtractionFailedThrowable("Failed to find type match in content string: '$contentString'")
        }
    }

    @Throws(TransactionDataExtractionFailedThrowable::class)
    private fun extractTimestamp(messageBody: String): LocalDateTime {
        val timestampMatchGroups = timestampRegex.find(messageBody)?.groupValues
        logD { "Timestamp match groups - $timestampMatchGroups" }
        println("Timestamp match groups - $timestampMatchGroups")
        if (timestampMatchGroups.isNullOrEmpty())
            throw TransactionDataExtractionFailedThrowable("Failed to find timestamp match groups in message body: '$messageBody'")

        val dateString = timestampMatchGroups.getOrNull(1)
            ?: throw TransactionDataExtractionFailedThrowable("Failed to get date string from match groups: '$timestampMatchGroups'")
        logD { "Date string - $dateString" }
        println("Date string - $dateString")
        val timeString = timestampMatchGroups.getOrNull(3).orEmpty()
        logD { "Time string - $timeString" }
        println("Time string - $timeString")
        val timestampPattern = buildString {
            val isFirstSubUnitA4DigitYear = dateString.substringBefore('-').length == 4

            var subUnitCount = 0

            // Build date section
            if (isFirstSubUnitA4DigitYear) {
                dateString.forEach { dateChar ->
                    if (dateChar.isDigit()) {
                        when (subUnitCount) {
                            0 -> append("y")
                            1 -> append("M")
                            else -> append("d")
                        }
                    } else {
                        // separator
                        append(dateChar)
                        subUnitCount += 1
                    }
                }
            } else {
                dateString.forEach { dateChar ->
                    if (dateChar.isDigit()) {
                        when (subUnitCount) {
                            0 -> append("d")
                            1 -> append("M")
                            else -> append("y")
                        }
                    } else {
                        // separator
                        append(dateChar)
                        subUnitCount += 1
                    }
                }
            }

            subUnitCount = 0
            timeString.forEach {
                if (it.isDigit()) {
                    when (subUnitCount) {
                        // Sub unit count 1 -> HH section
                        1 -> append("H")
                        // Sub unit count 1 -> mm section
                        2 -> append("m")
                        // Sub unit count 1 -> ss section
                        else -> append("s")
                    }
                } else {
                    // Non digit value -> ':' separator
                    // increment subUnitCount
                    append(it)
                    subUnitCount += 1
                }
            }
        }.trim()

        println("Built parse pattern - $timestampPattern")
        val timestampString = "$dateString${timeString}".trim()
        logI { "Timestamp string - $timestampString" }
        println("Timestamp string - $timestampString")

        return DateUtil.parseDateTime(
            value = timestampString,
            formatter = DateUtil.Formatters.formatterWithDefault(timestampPattern)
        )
            ?: throw TransactionDataExtractionFailedThrowable("Failed to parse '$timestampString' with built pattern '$timestampPattern'")
    }

    /*private fun getNextWord(source: String, searchWord: String, count: Int = 1): String {
        val splits = source.split(searchWord, ignoreCase = true, limit = 2)
        splits.getOrNull(1)?.let { nextGroup ->
            val workSplitRegex = "/[^0-9a-zA-Z]+/gi".toRegex()
            return nextGroup.trim().split(workSplitRegex, count).joinToString(String.WhiteSpace)
        }

        return String.Empty
    }*/

    private fun processText(string: String): List<String> {
        var message = string.lowercase(LocaleUtil.defaultLocale)
        // remove '-'
        message = message.replace("-".toRegex(), String.Empty)
        // remove '!'
        message = message.replace("!".toRegex(), "")
        // remove ':'
        message = message.replace(":".toRegex(), " ")
        // remove '/'
        message = message.replace("/".toRegex(), "")
        // remove '='
        message = message.replace("=".toRegex(), " ")
        // remove '{}'
        message = message.replace("[{}]".toRegex(), " ")
        // remove \n
        message = message.replace("\n".toRegex(), " ")
        // remove \r
        message = message.replace("\r".toRegex(), " ")
        // remove 'ending'
        message = message.replace("ending ".toRegex(), "")
        // replace 'x'
        message = message.replace("x|[*]".toRegex(), "")
        // // remove 'is' 'with'
        // message = message.replace(\bis\b|\bwith\b, '')
        // replace 'is'
        message = message.replace("(?i)is ".toRegex(), "")
        // replace 'with'
        message = message.replace("(?i)with ".toRegex(), "")
        // remove 'no.'
        message = message.replace("(?i)no. ".toRegex(), "")
        // replace all ac, acct, account with ac
        message = message.replace("(?i)\bac\b|\bacct\b|\baccount\b".toRegex(), "ac")
        // replace all 'rs' with 'rs. '
        message = message.replace("(?i)rs(?=\\w)".toRegex(), "rs. ")
        // replace all 'rs ' with 'rs. '
        message = message.replace("(?i)rs ".toRegex(), "rs. ")
        // replace all inr with rs.
        message = message.replace("(?i)inr(?=\\w)".toRegex(), "rs. ")
        //
        message = message.replace("(?i)inr ".toRegex(), "rs. ")
        // replace all 'rs. ' with 'rs.'
        message = message.replace("(?i)rs. ".toRegex(), "rs.")
        // replace all 'rs.' with 'rs. '
        message = message.replace("(?i)rs.(?=\\w)".toRegex(), "rs. ")
        // replace all 'debited' with ' debited '
        message = message.replace("(?i)debited".toRegex(), " debited ")
        // replace all 'credited' with ' credited '
        message = message.replace("(?i)credited".toRegex(), " credited ")
        // combine words
        /*combinedWords.forEach((word) => {
            message = message.replace(word.regex, word.word);
        })*/
        return message.split(" ").filter { it.isNotEmpty() }
    }

    /*companion object {
        val upiKeywords: List<String>
            get() = listOf("upi", "ref no", "upi ref", "upi ref no")
    }*/
}

private const val ENGLISH_CHARS_PATTERN = "(?i)(?!...)(?!..\$)[^\\W][\\w.]{0,29}\$"

private const val ORG_ADDRESS_PATTERN = "(?i)\\w{2}-\\w{6}"

private const val CREDIT_PATTERN =
    "(?i)(?:credited|credit|deposited|added|received|refund|repayment)"
private const val DEBIT_PATTERN = "(?i)(?:debited|debit|deducted)"
private const val MISC_PAYMENT_PATTERN =
    "(?i)(?:payment|spent|paid|used\\s+at|charged|transaction\\son|transaction\\sfee|tran|booked|purchased|sent|sent\\s+to|purchase\\s+of)"
private const val TIMESTAMP_PATTERN =
    "(?i)on\\s+(\\d{2,4}-\\d{2}(-\\d{2})?(:\\d{2}:\\d{2}:\\d{2})?)"