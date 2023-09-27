package dev.ridill.rivo.transactionGroups.data.repository

import dev.ridill.rivo.transactionGroups.data.local.TransactionGroupDao
import dev.ridill.rivo.transactionGroups.data.local.entity.TransactionGroupEntity
import dev.ridill.rivo.transactionGroups.data.toTxGroupDetails
import dev.ridill.rivo.transactionGroups.domain.model.TxGroupDetails
import dev.ridill.rivo.transactionGroups.domain.repository.TxGroupDetailsRepository
import dev.ridill.rivo.transactions.data.local.TransactionDao
import dev.ridill.rivo.transactions.data.local.relations.TransactionDetails
import dev.ridill.rivo.transactions.data.toTransactionListItem
import dev.ridill.rivo.transactions.domain.model.TransactionListItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.time.LocalDateTime

class TxGroupDetailsRepositoryImpl(
    private val dao: TransactionGroupDao,
    private val transactionDao: TransactionDao
) : TxGroupDetailsRepository {

    override fun getGroupDetailsFlowById(id: Long): Flow<TxGroupDetails?> = dao
        .getGroupDetailsWithAggregateExpenditureById(id).map { it?.toTxGroupDetails() }

    override suspend fun saveGroup(
        id: Long,
        name: String,
        createdTimestamp: LocalDateTime,
        excluded: Boolean
    ): Long = withContext(Dispatchers.IO) {
        val entity = TransactionGroupEntity(
            id = id,
            name = name,
            createdTimestamp = createdTimestamp,
            isExcluded = excluded
        )
        dao.insert(entity).first()
    }

    override fun getTransactionsForGroup(id: Long): Flow<List<TransactionListItem>> = transactionDao
        .getTransactionsList(
            groupId = id
        ).map { it.map(TransactionDetails::toTransactionListItem) }
}