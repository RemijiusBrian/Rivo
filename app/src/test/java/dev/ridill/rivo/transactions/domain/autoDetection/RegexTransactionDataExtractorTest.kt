package dev.ridill.rivo.transactions.domain.autoDetection

import org.junit.After
import org.junit.Before
import org.junit.Test

class RegexTransactionDataExtractorTest {

    private lateinit var regexExtractor: TransactionDataExtractor
    private lateinit var messageList: List<String>

    @Before
    fun setUp() {
        regexExtractor = RegexTransactionDataExtractor()
        messageList = listOf(
            """Amt Sent Rs.250.00
                From HDFC Bank A/C *1643
                To H R FRUITS
                On 27-03
                Ref 408718670922 
                Not You? Call 18002586161/SMS BLOCK UPI to 7308080808
            """.trimIndent()
        )
    }

    @After
    fun teardown() {
        messageList = emptyList()
    }

    @Test
    fun testExtractDataAgainstMultipleMessages_testPasses() {
        messageList.forEach { message ->
            val data = regexExtractor.extractData(message)
            println("Extracted Data - $data")
        }
    }
}