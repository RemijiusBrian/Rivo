package dev.ridill.rivo.folders.domain.repository

import androidx.paging.PagingData
import dev.ridill.rivo.core.domain.util.Empty
import dev.ridill.rivo.folders.domain.model.Folder
import dev.ridill.rivo.folders.domain.model.FolderUIModel
import kotlinx.coroutines.flow.Flow

interface FoldersListRepository {
    fun getFoldersWithAggregateList(): Flow<PagingData<FolderUIModel>>
    fun getFoldersListPaged(searchQuery: String = String.Empty): Flow<PagingData<Folder>>
    suspend fun getFolderById(id: Long): Folder?
    fun getFolderByIdFlow(id: Long): Flow<Folder?>
}