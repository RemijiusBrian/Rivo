package dev.ridill.rivo.transactions.domain.repository

import dev.ridill.rivo.schedules.domain.model.Schedule
import dev.ridill.rivo.schedules.domain.model.ScheduleRepeatMode
import dev.ridill.rivo.transactions.domain.model.Transaction
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime
import java.util.Currency

interface AddEditTransactionRepository {
    fun getCurrencyPreference(dateTime: LocalDateTime): Flow<Currency>
    suspend fun getTransactionById(id: Long): Transaction?
    fun getAmountRecommendations(): Flow<List<Long>>
    suspend fun saveTransaction(transaction: Transaction): Long
    suspend fun deleteTransaction(id: Long)
    suspend fun toggleExclusionById(id: Long, excluded: Boolean)
    suspend fun getScheduleById(id: Long): Schedule?
    suspend fun deleteSchedule(id: Long)
    suspend fun saveSchedule(transaction: Transaction, repeatMode: ScheduleRepeatMode)
}