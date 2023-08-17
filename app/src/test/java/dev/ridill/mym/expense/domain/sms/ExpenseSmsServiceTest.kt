package dev.ridill.mym.expense.domain.sms

import dev.ridill.mym.core.domain.util.log
import org.junit.Before
import org.junit.Test

class ExpenseSmsServiceTest {

    private lateinit var smsService: ExpenseSmsService

    @Before
    fun setup() {
        smsService = ExpenseSmsService()
    }

    @Test
    fun test() {
        val content = "HDFC Bank: Rs 56.00 debited from a/c **1643 on 17-08-23 to VPA dhanush911gtroksbi(UPI Ref No 322949577034). Not you? Call on 18002586161 to report"

        val isDebitSms = smsService.isDebitSms(content)
        log { "Is Debit SMS - $isDebitSms" }

        val amount = smsService.extractAmount(content)
        log { "Amount - $amount" }
        val merchant = smsService.extractMerchant(content)
        log { "Merchant - $merchant" }
    }
}