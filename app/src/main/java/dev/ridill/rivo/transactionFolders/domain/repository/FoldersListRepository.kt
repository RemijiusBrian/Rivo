package dev.ridill.rivo.transactionFolders.domain.repository

import androidx.paging.PagingData
import dev.ridill.rivo.core.domain.model.ListMode
import dev.ridill.rivo.core.domain.util.Empty
import dev.ridill.rivo.transactionFolders.domain.model.TransactionFolder
import dev.ridill.rivo.transactionFolders.domain.model.TransactionFolderDetails
import kotlinx.coroutines.flow.Flow

interface FoldersListRepository {
    fun getFoldersWithAggregateList(): Flow<PagingData<TransactionFolderDetails>>
    fun getFoldersListMode(): Flow<ListMode>
    suspend fun updateFoldersListMode(listMode: ListMode)
    fun getFoldersList(searchQuery: String = String.Empty): Flow<List<TransactionFolder>>
}