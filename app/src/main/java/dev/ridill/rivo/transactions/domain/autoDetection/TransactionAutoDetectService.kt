package dev.ridill.rivo.transactions.domain.autoDetection

import android.telephony.SmsMessage
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
import dev.ridill.rivo.core.domain.notification.NotificationHelper
import dev.ridill.rivo.core.domain.util.DateUtil
import dev.ridill.rivo.di.ApplicationScope
import dev.ridill.rivo.transactions.domain.model.Transaction
import dev.ridill.rivo.transactions.domain.repository.TransactionRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class TransactionAutoDetectService(
    private val extractor: TransactionDataExtractor,
    private val transactionRepo: TransactionRepository,
    private val notificationHelper: NotificationHelper<Transaction>,
    @ApplicationScope private val applicationScope: CoroutineScope
) {
    fun detectTransactionsFromMessages(messages: List<SmsMessage>) = applicationScope.launch {
        val orgRegex = ORG_ADDRESS_PATTERN.toRegex()
        val messagesFromOrg = messages.filter { orgRegex.matches(it.displayMessageBody.orEmpty()) }
        val dateTimeNow = DateUtil.now()
        for (message in messagesFromOrg) {
            try {
                val data = extractor.extractData(message.messageBody)
                if (data.paymentTimestamp.isAfter(dateTimeNow)) continue

                val insertedTx = transactionRepo.saveTransaction(
                    amount = data.amount,
                    timestamp = data.paymentTimestamp,
                    note = data.secondParty.orEmpty(),
                    type = data.transactionType
                )

                notificationHelper.postNotification(
                    id = insertedTx.id.hashCode(),
                    data = insertedTx
                )
            } catch (t: TransactionDataExtractionFailedThrowable) {
                Firebase.crashlytics.recordException(t)
            } catch (t: Throwable) {
                Firebase.crashlytics.recordException(t)
            }
        }
    }
}

private const val ORG_ADDRESS_PATTERN = "(?i)\\w{2}-\\w{6}"