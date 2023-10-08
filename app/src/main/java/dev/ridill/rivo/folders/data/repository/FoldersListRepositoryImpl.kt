package dev.ridill.rivo.folders.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import dev.ridill.rivo.core.domain.model.ListMode
import dev.ridill.rivo.core.domain.model.SortOrder
import dev.ridill.rivo.core.domain.util.UtilConstants
import dev.ridill.rivo.folders.data.local.FolderDao
import dev.ridill.rivo.folders.data.local.entity.FolderEntity
import dev.ridill.rivo.folders.data.local.relation.FolderAndAggregateAmount
import dev.ridill.rivo.folders.data.toFolder
import dev.ridill.rivo.folders.data.toFolderDetails
import dev.ridill.rivo.folders.domain.model.Folder
import dev.ridill.rivo.folders.domain.model.FolderDetails
import dev.ridill.rivo.folders.domain.model.FolderSortCriteria
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
    override fun getFoldersWithAggregateList(
        sortCriteria: FolderSortCriteria,
        sortOrder: SortOrder
    ): Flow<PagingData<FolderDetails>> = Pager(
        config = PagingConfig(pageSize = UtilConstants.DEFAULT_PAGE_SIZE)
    ) {
        when (sortCriteria) {
            FolderSortCriteria.BY_NAME -> when (sortOrder) {
                SortOrder.ASCENDING -> folderDao.getFoldersWithAggregateExpenditureSortedByNameAsc()
                SortOrder.DESCENDING -> folderDao.getFoldersWithAggregateExpenditureSortedByNameDesc()
            }

            FolderSortCriteria.BY_CREATED -> when (sortOrder) {
                SortOrder.ASCENDING -> folderDao.getFoldersWithAggregateExpenditureSortedByCreatedAsc()
                SortOrder.DESCENDING -> folderDao.getFoldersWithAggregateExpenditureSortedByCreatedDesc()
            }

            FolderSortCriteria.BY_AGGREGATE -> when (sortOrder) {
                SortOrder.ASCENDING -> folderDao.getFoldersWithAggregateExpenditureSortedByAggregateAsc()
                SortOrder.DESCENDING -> folderDao.getFoldersWithAggregateExpenditureSortedByAggregateDesc()
            }
        }
    }
        .flow
        .map { it.map(FolderAndAggregateAmount::toFolderDetails) }

    override fun getFoldersListSortCriteria(): Flow<FolderSortCriteria> = configDao
        .getFoldersListSortCriteria().map {
            FolderSortCriteria.valueOf(
                it ?: FolderSortCriteria.BY_AGGREGATE.name
            )
        }

    override fun getFoldersListSortOrder(): Flow<SortOrder> = configDao
        .getFoldersListSortOrder().map {
            SortOrder.valueOf(
                it ?: SortOrder.DESCENDING.name
            )
        }

    override suspend fun updateFoldersListSort(criteria: FolderSortCriteria, order: SortOrder) {
        val criteriaEntity = ConfigEntity(
            configKey = ConfigKeys.FOLDERS_LIST_SORT_CRITERIA,
            configValue = criteria.name
        )

        val orderEntity = ConfigEntity(
            configKey = ConfigKeys.FOLDERS_LIST_SORT_ORDER,
            configValue = order.name
        )

        configDao.insert(criteriaEntity, orderEntity)
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