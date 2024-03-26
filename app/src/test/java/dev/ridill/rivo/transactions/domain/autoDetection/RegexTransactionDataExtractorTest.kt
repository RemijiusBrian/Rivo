package dev.ridill.rivo.transactions.domain.autoDetection

import org.junit.Before
import org.junit.Test

class RegexTransactionDataExtractorTest {

    lateinit var regexExtractor: RegexTransactionDataExtractor

    @Before
    fun setUp() {
        regexExtractor = RegexTransactionDataExtractor()
    }

    @Test
    fun test() {
        val message = "Amt Sent Rs.100.00\n" +
                "From HDFC Bank A/C *1643\n" +
                "To MARVIN CLEMENT\n" +
                "On 26-03\n" +
                "Ref 408666163985\n" +
                "Not You? Call 18002586161/SMS BLOCK UPI to 7308080808"
        regexExtractor.extractData(message)
    }
}