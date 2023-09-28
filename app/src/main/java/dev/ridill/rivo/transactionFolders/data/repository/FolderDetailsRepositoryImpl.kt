package dev.ridill.rivo.transactionFolders.data.repository

import dev.ridill.rivo.transactionFolders.data.local.TransactionFolderDao
import dev.ridill.rivo.transactionFolders.data.local.entity.TransactionFolderEntity
import dev.ridill.rivo.transactionFolders.data.toTransactionFolderDetails
import dev.ridill.rivo.transactionFolders.domain.model.TransactionFolderDetails
import dev.ridill.rivo.transactionFolders.domain.repository.FolderDetailsRepository
import dev.ridill.rivo.transactions.data.local.TransactionDao
import dev.ridill.rivo.transactions.data.local.relations.TransactionDetails
import dev.ridill.rivo.transactions.data.toTransactionListItem
import dev.ridill.rivo.transactions.domain.model.TransactionListItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalDateTime

class FolderDetailsRepositoryImpl(
    private val dao: TransactionFolderDao,
    private val transactionDao: TransactionDao
) : FolderDetailsRepository {

    override fun getFolderDetailsById(id: Long): Flow<TransactionFolderDetails?> = dao
        .getFolderWithAggregateExpenditureById(id).map { it?.toTransactionFolderDetails() }

    override suspend fun saveFolder(
        id: Long,
        name: String,
        createdTimestamp: LocalDateTime,
        excluded: Boolean
    ): Long = withContext(Dispatchers.IO) {
        val entity = TransactionFolderEntity(
            id = id,
            name = name,
            createdTimestamp = createdTimestamp,
            isExcluded = excluded
        )
        dao.insert(entity).first()
    }

    override fun getTransactionsInFolder(folderId: Long): Flow<Map<LocalDate, List<TransactionListItem>>> =
        transactionDao.getTransactionsList(
            folderId = folderId
        )
            .map { it.map(TransactionDetails::toTransactionListItem) }
            .map { transactions ->
                transactions
                    .groupBy { it.date.withDayOfMonth(1) }
            }

    override suspend fun addTransactionsToFolderByIds(folderId: Long, transactionIds: List<Long>) =
        withContext(Dispatchers.IO) {
            transactionDao.setFolderIdToTransactionsByIds(
                transactionIds = transactionIds,
                folderId = folderId
            )
        }

    override suspend fun deleteFolderById(id: Long) = withContext(Dispatchers.IO) {
        dao.deleteFolderOnlyById(id)
    }

    override suspend fun deleteFolderWithTransactions(id: Long) = withContext(Dispatchers.IO) {
        dao.deleteFolderAndTransactionsById(id)
    }
}