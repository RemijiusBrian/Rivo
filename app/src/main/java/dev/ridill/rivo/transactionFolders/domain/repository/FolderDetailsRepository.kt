package dev.ridill.rivo.transactionFolders.domain.repository

import dev.ridill.rivo.transactionFolders.domain.model.TransactionFolderDetails
import dev.ridill.rivo.transactions.domain.model.TransactionListItem
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
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

    fun getTransactionsInFolder(folderId: Long): Flow<Map<LocalDate, List<TransactionListItem>>>
    suspend fun addTransactionsToFolderByIds(folderId: Long, transactionIds: List<Long>)
}