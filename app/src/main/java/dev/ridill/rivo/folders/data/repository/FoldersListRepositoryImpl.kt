package dev.ridill.rivo.folders.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.insertSeparators
import androidx.paging.map
import dev.ridill.rivo.core.domain.model.ListMode
import dev.ridill.rivo.core.domain.util.UtilConstants
import dev.ridill.rivo.folders.data.local.FolderDao
import dev.ridill.rivo.folders.data.local.entity.FolderEntity
import dev.ridill.rivo.folders.data.local.views.FolderAndAggregateAmountView
import dev.ridill.rivo.folders.data.toFolder
import dev.ridill.rivo.folders.data.toFolderDetails
import dev.ridill.rivo.folders.domain.model.Folder
import dev.ridill.rivo.folders.domain.model.FolderUIModel
import dev.ridill.rivo.folders.domain.repository.FoldersListRepository
import dev.ridill.rivo.settings.data.local.ConfigDao
import dev.ridill.rivo.settings.data.local.ConfigKeys
import dev.ridill.rivo.settings.data.local.entity.ConfigEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class FoldersListRepositoryImpl(
    private val folderDao: FolderDao,
    private val configDao: ConfigDao
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

    override fun getFoldersListMode(): Flow<ListMode> = configDao
        .getFoldersListMode().map {
            ListMode.valueOf(
                it ?: ListMode.GRID.name
            )
        }

    override suspend fun updateFoldersListMode(listMode: ListMode) {
        withContext(Dispatchers.IO) {
            configDao.insert(
                ConfigEntity(
                    configKey = ConfigKeys.FOLDERS_LIST_MODE,
                    configValue = listMode.name
                )
            )
        }
    }

    override fun getFoldersList(searchQuery: String): Flow<PagingData<Folder>> =
        Pager(
            config = PagingConfig(pageSize = UtilConstants.DEFAULT_PAGE_SIZE)
        ) { folderDao.getFoldersList(searchQuery) }
            .flow
            .map { it.map(FolderEntity::toFolder) }

    override suspend fun getFolderById(id: Long): Folder? = withContext(Dispatchers.IO) {
        folderDao.getFolderById(id)?.toFolder()
    }
}