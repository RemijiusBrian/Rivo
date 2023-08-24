package dev.ridill.mym.expense.domain.sms

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dagger.hilt.android.AndroidEntryPoint
import dev.ridill.mym.R
import dev.ridill.mym.core.domain.util.DateUtil
import dev.ridill.mym.core.domain.util.TextFormat
import dev.ridill.mym.di.ApplicationScope
import dev.ridill.mym.expense.domain.notification.ExpenseNotificationHelper
import dev.ridill.mym.expense.domain.repository.ExpenseRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
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
            service.getEntityExtractor().use { extractor ->
                extractor.downloadModelIfNeeded().await()
                if (!extractor.isModelDownloaded.await()) return@launch

                for (message in messages) {
                    val content = message.messageBody
                    if (!service.isDebitSms(content)) continue

                    val expenseDetails = service.extractExpenseDetails(extractor, content)
                        ?: continue
                    val amount = expenseDetails.amount
                    val merchant = expenseDetails.merchant
                        ?: context.getString(R.string.generic_merchant)

                    val insertedId = expenseRepository.cacheExpense(
                        id = null,
                        amount = amount,
                        note = merchant,
                        dateTime = DateUtil.now(),
                        tagId = null
                    )

                    notificationHelper.postNotification(
                        id = insertedId.toInt(),
                        title = context.getString(R.string.new_expenses_detected),
                        content = context.getString(
                            R.string.amount_spent_towards_merchant,
                            TextFormat.currency(amount),
                            merchant
                        )
                    )
                }
            }
        }
    }
}