package dev.ridill.rivo.folders.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.insertSeparators
import androidx.paging.map
import dev.ridill.rivo.core.domain.util.UtilConstants
import dev.ridill.rivo.folders.data.local.FolderDao
import dev.ridill.rivo.folders.data.local.entity.FolderEntity
import dev.ridill.rivo.folders.data.local.views.FolderAndAggregateAmountView
import dev.ridill.rivo.folders.data.toFolder
import dev.ridill.rivo.folders.data.toFolderDetails
import dev.ridill.rivo.folders.domain.model.Folder
import dev.ridill.rivo.folders.domain.model.FolderUIModel
import dev.ridill.rivo.folders.domain.repository.FoldersListRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class FoldersListRepositoryImpl(
    private val folderDao: FolderDao
) : FoldersListRepository {
    override fun getFoldersWithAggregateList(): Flow<PagingData<FolderUIModel>> = Pager(
        config = PagingConfig(pageSize = UtilConstants.DEFAULT_PAGE_SIZE)
    ) {
        folderDao.getFoldersWithAggregateExpenditure()
    }.flow
        .map { it.map(FolderAndAggregateAmountView::toFolderDetails) }
        .map { pagingData ->
            pagingData.map { FolderUIModel.FolderListItem(it) }
        }
        .map { pagingData ->
            pagingData
                .insertSeparators<FolderUIModel.FolderListItem, FolderUIModel>
                { before, after ->
                    if (before?.folderDetails?.aggregateType != after?.folderDetails?.aggregateType)
                        after?.folderDetails?.aggregateType
                            ?.let { FolderUIModel.AggregateTypeSeparator(it) }
                    else null
                }
        }

    override fun getFoldersListPaged(searchQuery: String): Flow<PagingData<Folder>> =
        Pager(
            config = PagingConfig(pageSize = UtilConstants.DEFAULT_PAGE_SIZE)
        ) { folderDao.getFoldersList(searchQuery) }
            .flow
            .map { it.map(FolderEntity::toFolder) }

    override suspend fun getFolderById(id: Long): Folder? = withContext(Dispatchers.IO) {
        folderDao.getFolderById(id)?.toFolder()
    }

    override fun getFolderByIdFlow(id: Long): Flow<Folder?> = folderDao.getFolderByIdFlow(id)
        .map { it?.toFolder() }
}