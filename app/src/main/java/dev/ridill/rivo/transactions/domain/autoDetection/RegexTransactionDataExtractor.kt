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
import dev.ridill.rivo.core.ui.util.TextFormat
import dev.ridill.rivo.transactions.domain.model.TransactionType
import java.time.LocalDateTime
import java.time.format.DateTimeFormatterBuilder
import java.time.temporal.ChronoField

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
    private val secondPartyStartRegex = SECOND_PARTY_START_PATTERN.toRegex()
    private val secondPartyEndRegex = SECOND_PARTY_END_PATTERN.toRegex()
    private val timestampRegex = TIMESTAMP_PATTERN.toRegex()

    override fun isSupportedLanguage(message: String): Boolean =
        englishCharsRegex.matchEntire(message) != null

    override fun isOriginValidOrg(originatingAddress: String): Boolean =
        orgAddressRegex.matches(originatingAddress)

    @Throws(
        AmountExtractionFailedThrowable::class,
        TransactionTypeExtractionFailedThrowable::class,
        TransactionNoteBuildFailedThrowable::class,
        TimestampExtractionFailedThrowable::class,
        TransactionDataExtractionFailedThrowable::class
    )
    override fun extractData(messageBody: String): ExtractedTransactionData {
        val processedContent = processText(messageBody)

        val amount = extractTransactionAmount(processedContent)
        logD { "Extracted amount - $amount" }
        println("Extracted amount - $amount")
        val transactionType = extractTransactionType(processedContent)
        logD { "Extracted type - $transactionType" }
        println("Extracted type - $transactionType")
        val note = buildNote(processedContent, transactionType)
        logD { "Transaction note - $note" }
        println("note - $note")
        val timestamp = extractTimestamp(messageBody)
        logD { "Extracted timestamp - $timestamp" }
        println("Extracted timestamp - $timestamp")

        return ExtractedTransactionData(
            amount = amount,
            paymentTimestamp = timestamp,
            transactionType = transactionType,
            note = note
        )
    }

    @Throws(AmountExtractionFailedThrowable::class)
    private fun extractTransactionAmount(content: List<String>): Double {
        println("Content for amount extraction - $content")
        val index = content.indexOfFirst { it.startsWith("rs.") }
            .takeIf { it > -1 }
            ?: throw AmountExtractionFailedThrowable("Failed to find 'rs' keyword\nContent: $content")

        val amountString = content[index + 1]
        val amount = amountString
            .removePrefix("rs.")
            .replace(",", String.Empty)
            .let(TextFormat::parseNumber)
            ?: throw AmountExtractionFailedThrowable("Failed parsing amount string '$amountString' to double")

        return amount.ifNaN { Double.Zero }
    }

    @Throws(TransactionNoteBuildFailedThrowable::class)
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
            val secondPartyStartIndex = content.indexOfFirst { it.matches(secondPartyStartRegex) }
                .takeIf { it > -1 }
                ?: throw TransactionNoteBuildFailedThrowable("Failed to find start index of second party keyword\nContent: $joinedString")

            val secondPartyEndIndex = content
                .subList(secondPartyStartIndex + 1, content.size)
                .also { println("sublist to find end index - $it") }
                .indexOfFirst { it.matches(secondPartyEndRegex) }
                .also { println("Second party end index - $it") }
                .takeIf { it > -1 }
                ?: throw TransactionNoteBuildFailedThrowable("Failed to find ending index of second party\nContent: $joinedString")

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

    @Throws(TransactionTypeExtractionFailedThrowable::class)
    private fun extractTransactionType(content: List<String>): TransactionType {
        val contentString = content.joinToString(String.WhiteSpace)

        return when (true) {
            contentString.contains(debitRegex) -> TransactionType.DEBIT
            contentString.contains(miscPaymentRegex) -> TransactionType.DEBIT
            contentString.contains(creditRegex) -> TransactionType.CREDIT
            else -> throw TransactionTypeExtractionFailedThrowable("Failed to find type match in\nContent: $contentString")
        }
    }

    @Throws(TimestampExtractionFailedThrowable::class)
    private fun extractTimestamp(messageBody: String): LocalDateTime {
        val timestampMatchGroups = timestampRegex.find(messageBody)?.groupValues
        logD { "Timestamp match groups - $timestampMatchGroups" }
        println("Timestamp match groups - $timestampMatchGroups")
        if (timestampMatchGroups.isNullOrEmpty())
            throw TimestampExtractionFailedThrowable("Failed to find timestamp match groups\nContent: $messageBody\n")

        val dateString = timestampMatchGroups.getOrNull(1)
            ?: throw TimestampExtractionFailedThrowable("Failed to get date string\nContent: $messageBody\nMatch Groups: '$timestampMatchGroups'")
        logD { "Date string - $dateString" }
        println("Date string - $dateString")
        val timeString = timestampMatchGroups.getOrNull(3).orEmpty()
        logD { "Time string - $timeString" }
        println("Time string - $timeString")
        val dateTimeNow = DateUtil.now()
        val dateTimeFormatterBuilder = DateTimeFormatterBuilder().apply {
            // Build date section
            val dateDelimiter = dateString.find { !it.isDigit() }
                ?.also {
                    println("Date Delimiter - $it")
                }
                ?: throw TimestampExtractionFailedThrowable("Failed to find date delimiter in date string: $dateString")
            val dateParts = dateString.split(dateDelimiter)
            val isFirstPartYear = dateParts[0].length == 4
            dateParts.forEachIndexed { index, part ->
                when (index) {
                    0 -> {
                        if (isFirstPartYear) {
                            val pattern = part.map { 'y' }.joinToString(String.Empty)
                            println("Adding first year part: $pattern")
                            appendPattern(pattern)
                        } else {
                            val pattern = part.map { 'd' }.joinToString(String.Empty)
                            println("Adding first day part: $pattern")
                            appendPattern(pattern)
                        }
                    }

                    2 -> {
                        appendPattern(dateDelimiter.toString())
                        if (isFirstPartYear) {
                            val pattern = part.map { 'd' }.joinToString(String.Empty)
                            println("Adding last day part: $pattern")
                            appendPattern(pattern)
                        } else {
                            println("Adding last year part")
                            optionalStart()
                            appendPattern("yyyy")
                            optionalEnd()
                            optionalStart()
                            appendValueReduced(ChronoField.YEAR, 2, 2, 1920)
                            optionalEnd()
                        }
                    }

                    else -> {
                        appendPattern(dateDelimiter.toString())
                        val pattern = part.map { 'M' }.joinToString(String.Empty)
                        println("Adding middle month: $pattern")
                        appendPattern(pattern)
                    }
                }
            }

            if (timeString.isNotEmpty()) {
                // Built time section
                val timeDelimiter = timeString.find { !it.isDigit() }
                    ?: throw TimestampExtractionFailedThrowable("Failed to find time delimiter in time string: $timeString")
                val timeParts = timeString.split(timeDelimiter)
                if (timeParts.isNotEmpty()) appendPattern(":")
                timeParts.forEachIndexed { index, part ->
                    when (index) {
                        0 -> {
                            appendPattern(part.map { 'H' }.joinToString(String.Empty))
                        }

                        1 -> {
                            appendPattern(timeDelimiter.toString())
                            appendPattern(part.map { 'm' }.joinToString(String.Empty))
                        }

                        else -> {
                            appendPattern(timeDelimiter.toString())
                            appendPattern(part.map { 's' }.joinToString(String.Empty))
                        }
                    }
                }
            }

            parseDefaulting(ChronoField.YEAR, dateTimeNow.year.toLong())
            parseDefaulting(ChronoField.MONTH_OF_YEAR, dateTimeNow.monthValue.toLong())
            parseDefaulting(ChronoField.DAY_OF_MONTH, dateTimeNow.dayOfMonth.toLong())
            parseDefaulting(ChronoField.HOUR_OF_DAY, dateTimeNow.hour.toLong())
            parseDefaulting(ChronoField.MINUTE_OF_HOUR, dateTimeNow.minute.toLong())
            parseDefaulting(ChronoField.SECOND_OF_MINUTE, dateTimeNow.second.toLong())
        }

        val timestampString = "$dateString${timeString}".trim()
        logI { "Timestamp string - $timestampString" }
        println("Timestamp string - $timestampString")

        return try {
            DateUtil.parseDateTime(
                value = timestampString,
                formatter = dateTimeFormatterBuilder.toFormatter()
            )
        } catch (t: Throwable) {
            throw TransactionDataExtractionFailedThrowable("Timestamp parse failed\nContent: $messageBody\nvalue - $timestampString | pattern - ${dateTimeFormatterBuilder.toFormatter()}\nThrowable message - ${t.message.orEmpty()}")
        }
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

private const val ENGLISH_CHARS_PATTERN = "(?i)^[a-zA-Z0-9 !@#$%^&*()-_+=/?\n]*\$"

private const val ORG_ADDRESS_PATTERN = "(?i)\\w{2}-\\w{6}"

private const val CREDIT_PATTERN =
    "(?i)(?:credited|credit|deposited|added|received|refund|repayment)"
private const val DEBIT_PATTERN = "(?i)(?:debited|debit|deducted)"
private const val MISC_PAYMENT_PATTERN =
    "(?i)(?:payment|spent|paid|used\\s+at|charged|transaction\\son|transaction\\sfee|tran|booked|purchased|sent|sent\\s+to|purchase\\s+of)"
private const val SECOND_PARTY_START_PATTERN = "(?i)at|to"
private const val SECOND_PARTY_END_PATTERN = "(?i)on|date"
private const val TIMESTAMP_PATTERN =
    "(?i)on\\s+(\\d{2,4}-\\d{2}(-\\d{2})?(:\\d{2}:\\d{2}:\\d{2})?)"

class UnsupportedLanguageThrowable(content: String) :
    TransactionDataExtractionFailedThrowable("AmountExtractionThrowable:Message is not supported language | content - $content")

class AmountExtractionFailedThrowable(message: String) :
    TransactionDataExtractionFailedThrowable("AmountExtractionThrowable:\n$message")

class TransactionTypeExtractionFailedThrowable(message: String) :
    TransactionDataExtractionFailedThrowable("TransactionTypeExtractionFailedThrowable:\n$message")

class TransactionNoteBuildFailedThrowable(message: String) :
    TransactionDataExtractionFailedThrowable("TransactionNoteBuildFailedThrowable:\n$message")

class TimestampExtractionFailedThrowable(message: String) :
    TransactionDataExtractionFailedThrowable("TimestampExtractionFailedThrowable:\n$message")