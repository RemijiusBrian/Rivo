package dev.ridill.rivo.folders.domain.repository

import androidx.paging.PagingData
import dev.ridill.rivo.core.domain.model.ListMode
import dev.ridill.rivo.core.domain.model.SortCriteria
import dev.ridill.rivo.core.domain.model.SortOrder
import dev.ridill.rivo.core.domain.util.Empty
import dev.ridill.rivo.folders.domain.model.FolderDetails
import dev.ridill.rivo.folders.domain.model.TransactionFolder
import kotlinx.coroutines.flow.Flow

interface FoldersListRepository {
    fun getFoldersWithAggregateList(
        sortCriteria: SortCriteria,
        sortOrder: SortOrder
    ): Flow<PagingData<FolderDetails>>

    fun getFoldersListSortCriteria(): Flow<SortCriteria>
    fun getFoldersListSortOrder(): Flow<SortOrder>
    suspend fun updateFoldersListSort(criteria: SortCriteria, order: SortOrder)
    fun getFoldersListMode(): Flow<ListMode>
    suspend fun updateFoldersListMode(listMode: ListMode)
    fun getFoldersList(searchQuery: String = String.Empty): Flow<List<TransactionFolder>>
}