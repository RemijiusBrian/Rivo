package dev.ridill.rivo.core.ui.util

import com.google.common.truth.Truth.assertThat
import dev.ridill.rivo.core.domain.util.Empty
import dev.ridill.rivo.core.domain.util.LocaleUtil

import org.junit.After
import org.junit.Before
import org.junit.Test

class TextFormatTest {

    private lateinit var amountString: String

    @Before
    fun setUp() {
        amountString = TextFormat.currency(2000.0, LocaleUtil.currencyForCode("INR"))
    }

    @After
    fun tearDown() {
        amountString = String.Empty
    }

    @Test
    fun testParseAmountStringWithCurrencySymbol_isNotNull() {
        println("Amount string - $amountString")
        val parsedNumber = TextFormat.parseNumber(amountString)
        assertThat(parsedNumber).isNotNull()
    }
}