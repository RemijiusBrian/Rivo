package dev.ridill.rivo.folders.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.insertSeparators
import androidx.paging.map
import dev.ridill.rivo.core.domain.util.UtilConstants
import dev.ridill.rivo.folders.data.local.FolderDao
import dev.ridill.rivo.folders.data.toFolderDetails
import dev.ridill.rivo.folders.domain.model.FolderDetails
import dev.ridill.rivo.folders.domain.repository.FolderDetailsRepository
import dev.ridill.rivo.transactions.data.local.TransactionDao
import dev.ridill.rivo.transactions.data.local.views.TransactionDetailsView
import dev.ridill.rivo.transactions.data.toEntity
import dev.ridill.rivo.transactions.data.toTransactionListItem
import dev.ridill.rivo.transactions.domain.model.TransactionListItem
import dev.ridill.rivo.transactions.domain.model.TransactionListItemUIModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class FolderDetailsRepositoryImpl(
    private val dao: FolderDao,
    private val transactionDao: TransactionDao
) : FolderDetailsRepository {
    override fun getFolderDetailsById(id: Long): Flow<FolderDetails?> = dao
        .getFolderWithAggregateExpenditureById(id).map { it?.toFolderDetails() }

    override fun getTransactionsInFolderPaged(
        folderId: Long
    ): Flow<PagingData<TransactionListItemUIModel>> = Pager(
        config = PagingConfig(pageSize = UtilConstants.DEFAULT_PAGE_SIZE)
    ) {
        transactionDao.getTransactionsPaged(
            startDate = null,
            endDate = null,
            type = null,
            showExcluded = false,
            tagIds = null,
            folderId = folderId
        )
    }
        .flow
        .map { it.map(TransactionDetailsView::toTransactionListItem) }
        .map { pagingData ->
            pagingData.map { TransactionListItemUIModel.TransactionItem(it) }
        }
        .map { pagingData ->
            pagingData
                .insertSeparators<TransactionListItemUIModel.TransactionItem, TransactionListItemUIModel>
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

    override suspend fun addTransactionsToFolderByIds(folderId: Long, transactionIds: Set<Long>) =
        withContext(Dispatchers.IO) {
            transactionDao.setFolderIdToTransactionsByIds(
                ids = transactionIds,
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
            transactionDao.setFolderIdToTransactionsByIds(setOf(transactionId), null)
        }

    override suspend fun addTransactionToFolder(transaction: TransactionListItem) {
        withContext(Dispatchers.IO) {
            transactionDao.upsert(transaction.toEntity())
        }
    }
}