package dev.ridill.rivo.scheduledTransaction.data.repository

import dev.ridill.rivo.core.data.db.RivoDatabase
import dev.ridill.rivo.core.domain.util.DateUtil
import dev.ridill.rivo.scheduledTransaction.data.local.ScheduledTransactionDao
import dev.ridill.rivo.scheduledTransaction.data.toEntity
import dev.ridill.rivo.scheduledTransaction.data.toScheduledTransaction
import dev.ridill.rivo.scheduledTransaction.domain.model.ScheduledTransaction
import dev.ridill.rivo.scheduledTransaction.domain.repository.ScheduledTransactionRepository
import dev.ridill.rivo.scheduledTransaction.domain.transactionScheduler.TransactionScheduler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate

class ScheduledTransactionRepositoryImpl(
    private val dao: ScheduledTransactionDao,
    private val scheduler: TransactionScheduler
) : ScheduledTransactionRepository {
    override suspend fun getTransactionById(id: Long): ScheduledTransaction? =
        withContext(Dispatchers.IO) {
            dao.getTransactionById(id)?.toScheduledTransaction()
        }

    override suspend fun updateNextPaymentDateForTransactionById(id: Long, nextDate: LocalDate) =
        withContext(Dispatchers.IO) {
            dao.updateNextReminderDateForTransactionById(id = id, nextDate = nextDate)
        }

    override suspend fun saveAndScheduleTransaction(transaction: ScheduledTransaction) {
        withContext(Dispatchers.IO) {
            val insertedId = dao.insert(transaction.toEntity()).first()
            scheduler.schedule(
                transaction = transaction.copy(
                    id = if (transaction.id > RivoDatabase.DEFAULT_ID_LONG) transaction.id
                    else insertedId
                )
            )
        }
    }

    override suspend fun cancelTransactionSchedule(transaction: ScheduledTransaction) {
        scheduler.cancel(transaction)
    }

    override suspend fun rescheduleAllTransactions() = withContext(Dispatchers.IO) {
        dao.getAllTransactionsAfterDate(DateUtil.dateNow())
            .forEach { entity ->
                scheduler.schedule(entity.toScheduledTransaction())
            }
    }
}