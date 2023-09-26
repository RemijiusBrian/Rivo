package dev.ridill.rivo.transactionGroups.data.repository

import dev.ridill.rivo.transactionGroups.data.local.TransactionGroupDao
import dev.ridill.rivo.transactionGroups.data.local.entity.TransactionGroupEntity
import dev.ridill.rivo.transactionGroups.data.toTxGroup
import dev.ridill.rivo.transactionGroups.domain.model.TxGroup
import dev.ridill.rivo.transactionGroups.domain.repository.TxGroupDetailsRepository
import dev.ridill.rivo.transactions.data.local.entity.TransactionEntity
import dev.ridill.rivo.transactions.data.toTransaction
import dev.ridill.rivo.transactions.domain.model.Transaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.time.LocalDateTime

class TxGroupDetailsRepositoryImpl(
    private val dao: TransactionGroupDao
) : TxGroupDetailsRepository {

    override fun getGroupDetailsFlowById(id: Long): Flow<TxGroup?> = dao
        .getGroupById(id).map { it?.toTxGroup() }

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

    override fun getTransactionsForGroup(id: Long): Flow<List<Transaction>> = dao
        .getTransactionsForGroup(id).map { it.map(TransactionEntity::toTransaction) }
}