package dev.ridill.rivo.folders.domain.repository

import androidx.paging.PagingData
import dev.ridill.rivo.folders.domain.model.FolderDetails
import dev.ridill.rivo.transactions.domain.model.TransactionListItem
import dev.ridill.rivo.transactions.domain.model.TransactionListItemUIModel
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

interface FolderDetailsRepository {
    fun getFolderDetailsById(id: Long): Flow<FolderDetails?>
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

    suspend fun addTransactionsToFolderByIds(folderId: Long, transactionIds: Set<Long>)
    suspend fun removeTransactionFromFolderById(transactionId: Long)
    suspend fun addTransactionToFolder(transaction: TransactionListItem)
}