package dev.ridill.rivo.transactions.data.repository

import dev.ridill.rivo.core.data.db.RivoDatabase
import dev.ridill.rivo.core.domain.util.Zero
import dev.ridill.rivo.core.domain.util.logD
import dev.ridill.rivo.core.domain.util.orZero
import dev.ridill.rivo.schedules.domain.model.Schedule
import dev.ridill.rivo.schedules.domain.model.ScheduleRepeatMode
import dev.ridill.rivo.schedules.domain.repository.SchedulesRepository
import dev.ridill.rivo.settings.domain.repositoty.CurrencyRepository
import dev.ridill.rivo.transactions.data.local.TransactionDao
import dev.ridill.rivo.transactions.data.toEntity
import dev.ridill.rivo.transactions.data.toTransaction
import dev.ridill.rivo.transactions.domain.model.Transaction
import dev.ridill.rivo.transactions.domain.repository.AddEditTransactionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import java.util.Currency
import kotlin.math.roundToLong

class AddEditTransactionRepositoryImpl(
    private val dao: TransactionDao,
    private val schedulesRepo: SchedulesRepository,
    private val currencyRepo: CurrencyRepository
) : AddEditTransactionRepository {
    override fun getCurrencyPreference(dateTime: LocalDateTime): Flow<Currency> = currencyRepo
        .getCurrencyForDateOrNext(
            date = dateTime.toLocalDate()
        )
        .distinctUntilChanged()

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
        dao.deleteById(id)
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
        val scheduledTx = Schedule(
            id = transaction.id,
            amount = transaction.amount.toDoubleOrNull().orZero(),
            note = transaction.note.ifEmpty { null },
            type = transaction.type,
            repeatMode = repeatMode,
            tagId = transaction.tagId,
            folderId = transaction.folderId,
            nextReminderDate = transaction.timestamp.toLocalDate()
        )
        val insertedId = schedulesRepo.saveSchedule(scheduledTx)
            .takeIf { it >= RivoDatabase.DEFAULT_ID_LONG }
            ?: transaction.id
        val transactionForSchedule = dao.getTransactionForScheduleAndDate(
            scheduleId = insertedId,
            date = transaction.timestamp.toLocalDate()
        )
        logD { "Tx for schedule - $transactionForSchedule" }
        // Set reminder if there isn't a payment corresponding to that schedule already
        if (transactionForSchedule == null)
            schedulesRepo.setScheduleReminder(
                schedule = scheduledTx.copy(
                    id = insertedId
                )
            )
    }
}

private const val RANGE_MIN_VALUE = 50L