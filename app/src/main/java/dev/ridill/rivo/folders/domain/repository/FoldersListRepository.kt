package dev.ridill.rivo.folders.domain.repository

import androidx.paging.PagingData
import dev.ridill.rivo.core.domain.model.ListMode
import dev.ridill.rivo.core.domain.model.SortOrder
import dev.ridill.rivo.core.domain.util.Empty
import dev.ridill.rivo.folders.domain.model.Folder
import dev.ridill.rivo.folders.domain.model.FolderDetails
import dev.ridill.rivo.folders.domain.model.FolderSortCriteria
import kotlinx.coroutines.flow.Flow

interface FoldersListRepository {
    fun getFoldersWithAggregateList(
        sortCriteria: FolderSortCriteria,
        sortOrder: SortOrder
    ): Flow<PagingData<FolderDetails>>

    fun getFoldersListSortCriteria(): Flow<FolderSortCriteria>
    fun getFoldersListSortOrder(): Flow<SortOrder>
    suspend fun updateFoldersListSort(criteria: FolderSortCriteria, order: SortOrder)
    fun getFoldersListMode(): Flow<ListMode>
    suspend fun updateFoldersListMode(listMode: ListMode)
    fun getFoldersList(searchQuery: String = String.Empty): Flow<PagingData<Folder>>
    suspend fun getFolderById(id: Long): Folder?
}