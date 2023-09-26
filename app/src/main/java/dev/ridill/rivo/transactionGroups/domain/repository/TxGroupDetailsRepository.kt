package dev.ridill.rivo.transactionGroups.domain.repository

import dev.ridill.rivo.transactionGroups.domain.model.TxGroup
import dev.ridill.rivo.transactions.domain.model.Transaction
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

interface TxGroupDetailsRepository {
    fun getGroupDetailsFlowById(id: Long): Flow<TxGroup?>
    suspend fun saveGroup(
        id: Long,
        name: String,
        createdTimestamp: LocalDateTime,
        excluded: Boolean
    ): Long

    fun getTransactionsForGroup(id: Long): Flow<List<Transaction>>
}