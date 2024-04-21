package dev.ridill.rivo.transactions.data.repository

import androidx.room.withTransaction
import dev.ridill.rivo.core.data.db.RivoDatabase
import dev.ridill.rivo.core.domain.util.Zero
import dev.ridill.rivo.core.domain.util.logI
import dev.ridill.rivo.core.domain.util.orZero
import dev.ridill.rivo.schedules.domain.model.Schedule
import dev.ridill.rivo.schedules.domain.model.ScheduleRepeatMode
import dev.ridill.rivo.schedules.domain.repository.SchedulesRepository
import dev.ridill.rivo.transactions.data.local.TransactionDao
import dev.ridill.rivo.transactions.data.toEntity
import dev.ridill.rivo.transactions.data.toTransaction
import dev.ridill.rivo.transactions.domain.model.Transaction
import dev.ridill.rivo.transactions.domain.repository.AddEditTransactionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlin.math.roundToLong

class AddEditTransactionRepositoryImpl(
    private val db: RivoDatabase,
    private val dao: TransactionDao,
    private val schedulesRepo: SchedulesRepository
) : AddEditTransactionRepository {
    override suspend fun getTransactionById(id: Long): Transaction? =
        withContext(Dispatchers.IO) {
            dao.getTransactionById(id)?.toTransaction()
        }

    override fun getAmountRecommendations(): Flow<List<Long>> = dao.getTransactionAmountRange()
        .map { (upperLimit, lowerLimit) ->
            val roundedUpper = ((upperLimit.roundToLong() / 10) * 10)
                .coerceAtLeast(RANGE_MIN_VALUE)
            val roundedLower = ((lowerLimit.roundToLong() / 10) * 10)
                .coerceAtLeast(RANGE_MIN_VALUE)

            val range = roundedUpper - roundedLower

            if (range == Long.Zero) buildList {
                repeat(3) {
                    add(RANGE_MIN_VALUE * (it + 1))
                }
            }
            else listOf(roundedLower, roundedLower + (range / 2), roundedUpper)
        }

    override suspend fun saveTransaction(transaction: Transaction): Long =
        withContext(Dispatchers.IO) {
            dao.insert(transaction.toEntity()).first()
        }

    override suspend fun deleteTransaction(id: Long) = withContext(Dispatchers.IO) {
        db.withTransaction {
            val transaction = dao.getTransactionById(id)
                ?: return@withTransaction
            dao.delete(transaction)
            logI { "Tx $transaction deleted" }

            // Update lastPaid and nextReminder dates for associated schedule
            if (transaction.scheduleId == null) return@withTransaction

            val schedule = schedulesRepo.getScheduleById(transaction.scheduleId)
                ?: return@withTransaction
            logI { "Found schedule for tx - $schedule" }

            // Check if deleted transaction is the same month as schedule lastPaidDate
            val isTxTimestampAndScheduleLastPaidDateSameMonth = schedule.lastPaidDate
                ?.month == transaction.timestamp.month

            if (isTxTimestampAndScheduleLastPaidDateSameMonth) {
                // Get latest payment date for schedule
                logI { "Tx same mont as schedule last paid date" }
                val newLastPaymentDate = schedulesRepo
                    .getLastTransactionTimestampForSchedule(schedule.id)
                logI { "Latest tx date for schedule - $newLastPaymentDate" }
                // calculate next reminder from last payment date
                val prevReminderDate = schedule.nextReminderDate
                    ?.let {
                        schedulesRepo.getPrevReminderFromDate(it, schedule.repeatMode)
                    } ?: schedule.lastPaidDate

                logI { "New prev reminder date for schedule - $prevReminderDate" }
                // update schedule and set new reminder for next date
                schedulesRepo.saveScheduleAndSetReminder(
                    schedule.copy(
                        lastPaidDate = newLastPaymentDate,
                        nextReminderDate = prevReminderDate
                    )
                )
            }
        }
    }

    override suspend fun toggleExclusionById(id: Long, excluded: Boolean) =
        withContext(Dispatchers.IO) {
            dao.toggleExclusionByIds(setOf(id), excluded)
        }

    override suspend fun getScheduleById(id: Long): Schedule? =
        schedulesRepo.getScheduleById(id)

    override suspend fun deleteSchedule(id: Long) =
        schedulesRepo.deleteScheduleById(id)

    override suspend fun saveSchedule(
        transaction: Transaction,
        repeatMode: ScheduleRepeatMode
    ) {
        val schedule = schedulesRepo.getScheduleById(transaction.id)
            ?.copy(
                amount = transaction.amount.toDoubleOrNull().orZero(),
                note = transaction.note.ifEmpty { null },
                type = transaction.type,
                repeatMode = repeatMode,
                tagId = transaction.tagId,
                folderId = transaction.folderId,
                nextReminderDate = transaction.timestamp
            ) ?: Schedule.fromTransaction(transaction, repeatMode)

        schedulesRepo.saveScheduleAndSetReminder(schedule)
    }
}

private const val RANGE_MIN_VALUE = 50L