package dev.ridill.mym.expense.domain.sms

import org.junit.Before

class ExpenseSmsServiceTest {

    private lateinit var smsService: ExpenseSmsService

    @Before
    fun setup() {
        smsService = ExpenseSmsService()
    }
}