package dev.ridill.rivo.transactionFolders.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.insertSeparators
import androidx.paging.map
import dev.ridill.rivo.core.domain.util.UtilConstants
import dev.ridill.rivo.transactionFolders.data.local.TransactionFolderDao
import dev.ridill.rivo.transactionFolders.data.local.entity.TransactionFolderEntity
import dev.ridill.rivo.transactionFolders.data.toTransactionFolderDetails
import dev.ridill.rivo.transactionFolders.domain.model.TransactionFolderDetails
import dev.ridill.rivo.transactionFolders.domain.repository.FolderDetailsRepository
import dev.ridill.rivo.transactions.data.local.TransactionDao
import dev.ridill.rivo.transactions.data.local.relations.TransactionDetails
import dev.ridill.rivo.transactions.data.toEntity
import dev.ridill.rivo.transactions.data.toTransactionListItem
import dev.ridill.rivo.transactions.domain.model.TransactionListItem
import dev.ridill.rivo.transactions.domain.model.TransactionListItemUIModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
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

    override fun getPagedTransactionsInFolder(
        folderId: Long
    ): Flow<PagingData<TransactionListItemUIModel>> = Pager(
        config = PagingConfig(pageSize = UtilConstants.DEFAULT_PAGE_SIZE)
    ) { transactionDao.getTransactionsListPaginated(folderId = folderId) }
        .flow
        .map { it.map(TransactionDetails::toTransactionListItem) }
        .map { pagingData ->
            pagingData.map { TransactionListItemUIModel.TransactionItem(it) }
        }
        .map {
            it.insertSeparators<TransactionListItemUIModel.TransactionItem, TransactionListItemUIModel>
            { before, after ->
                if (before?.transaction?.timestamp
                        ?.withDayOfMonth(1)
                        ?.toLocalDate()
                    != after?.transaction?.timestamp
                        ?.withDayOfMonth(1)
                        ?.toLocalDate()
                ) after?.transaction?.timestamp
                    ?.withDayOfMonth(1)
                    ?.toLocalDate()
                    ?.let { localDate ->
                        TransactionListItemUIModel.DateSeparator(localDate)
                    } else null
            }
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

    override suspend fun removeTransactionFromFolderById(transactionId: Long) =
        withContext(Dispatchers.IO) {
            transactionDao.setFolderIdToTransactionsByIds(listOf(transactionId), null)
        }

    override suspend fun addTransactionToFolder(transaction: TransactionListItem) {
        withContext(Dispatchers.IO) {
            transactionDao.insert(transaction.toEntity())
        }
    }
}