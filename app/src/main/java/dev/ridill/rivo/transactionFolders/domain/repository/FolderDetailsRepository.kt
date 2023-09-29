package dev.ridill.rivo.transactionFolders.domain.repository

import androidx.paging.PagingData
import dev.ridill.rivo.transactionFolders.domain.model.TransactionFolderDetails
import dev.ridill.rivo.transactions.domain.model.TransactionListItem
import dev.ridill.rivo.transactions.domain.model.TransactionListItemUIModel
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

interface FolderDetailsRepository {
    fun getFolderDetailsById(id: Long): Flow<TransactionFolderDetails?>
    suspend fun deleteFolderById(id: Long)
    suspend fun deleteFolderWithTransactions(id: Long)
    suspend fun saveFolder(
        id: Long,
        name: String,
        createdTimestamp: LocalDateTime,
        excluded: Boolean
    ): Long

    fun getPagedTransactionsInFolder(
        folderId: Long
    ): Flow<PagingData<TransactionListItemUIModel>>

    suspend fun addTransactionsToFolderByIds(folderId: Long, transactionIds: List<Long>)
    suspend fun removeTransactionFromFolderById(transactionId: Long)
    suspend fun addTransactionToFolder(transaction: TransactionListItem)
}