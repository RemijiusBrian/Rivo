package dev.ridill.mym.expense.domain.sms

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dagger.hilt.android.AndroidEntryPoint
import dev.ridill.mym.R
import dev.ridill.mym.core.domain.util.DateUtil
import dev.ridill.mym.core.domain.util.TextFormatter
import dev.ridill.mym.di.ApplicationScope
import dev.ridill.mym.expense.domain.notification.ExpenseNotificationHelper
import dev.ridill.mym.expense.domain.repository.ExpenseRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ExpenseSmsReceiver : BroadcastReceiver() {

    @Inject
    lateinit var service: ExpenseSmsService

    @Inject
    lateinit var expenseRepository: ExpenseRepository

    @ApplicationScope
    @Inject
    lateinit var applicationScope: CoroutineScope

    @Inject
    lateinit var notificationHelper: ExpenseNotificationHelper

    override fun onReceive(context: Context, intent: Intent) {
        if (!service.isSmsActionValid(intent.action)) return

        applicationScope.launch {
            val messages = service.getSmsFromIntent(intent)
            for (message in messages) {
                if (!service.isBankSms(message.originatingAddress)) continue

                val content = message.messageBody
                val amount = service.extractAmount(content)?.toDoubleOrNull() ?: continue
                val merchant = service.extractMerchant(content) ?: continue

                val insertedId = expenseRepository.cacheExpense(
                    id = null,
                    amount = amount,
                    note = merchant,
                    dateTime = DateUtil.now(),
                    tagId = null
                )

                notificationHelper.postNotification(
                    id = insertedId.toInt(),
                    title = context.getString(R.string.expense_added),
                    content = context.getString(
                        R.string.amount_spent_at_merchant,
                        TextFormatter.currency(amount),
                        merchant
                    )
                )
            }
        }
    }
}