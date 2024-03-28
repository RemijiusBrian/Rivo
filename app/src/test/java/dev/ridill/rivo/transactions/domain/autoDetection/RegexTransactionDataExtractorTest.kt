package dev.ridill.rivo.transactions.domain.autoDetection

import org.junit.Before
import org.junit.Test

class RegexTransactionDataExtractorTest {

    private lateinit var regexExtractor: TransactionDataExtractor

    @Before
    fun setUp() {
        regexExtractor = RegexTransactionDataExtractor()
    }

    @Test
    fun test() {
        val message = "Amt Sent Rs.250.00\n" +
                "From HDFC Bank A/C *1643\n" +
                "To H R FRUITS\n" +
                "On 27-03\n" +
                "Ref 408718670922\n" +
                "Not You? Call 18002586161/SMS BLOCK UPI to 7308080808"
        val data = regexExtractor.extractData(message)
        println("Extracted Data - $data")
    }
}