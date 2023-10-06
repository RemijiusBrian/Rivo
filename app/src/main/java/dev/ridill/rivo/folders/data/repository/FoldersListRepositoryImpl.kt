package dev.ridill.rivo.folders.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import dev.ridill.rivo.core.domain.model.ListMode
import dev.ridill.rivo.core.domain.model.SortCriteria
import dev.ridill.rivo.core.domain.model.SortOrder
import dev.ridill.rivo.core.domain.util.UtilConstants
import dev.ridill.rivo.folders.data.local.FolderDao
import dev.ridill.rivo.folders.data.local.entity.FolderEntity
import dev.ridill.rivo.folders.data.local.relation.FolderAndAggregateAmount
import dev.ridill.rivo.folders.data.toTransactionFolder
import dev.ridill.rivo.folders.data.toTransactionFolderDetails
import dev.ridill.rivo.folders.domain.model.FolderDetails
import dev.ridill.rivo.folders.domain.model.TransactionFolder
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
        sortCriteria: SortCriteria,
        sortOrder: SortOrder
    ): Flow<PagingData<FolderDetails>> = Pager(
        config = PagingConfig(pageSize = UtilConstants.DEFAULT_PAGE_SIZE)
    ) {
        when (sortCriteria) {
            SortCriteria.BY_NAME -> when (sortOrder) {
                SortOrder.ASCENDING -> folderDao.getFoldersWithAggregateExpenditureSortedByNameAsc()
                SortOrder.DESCENDING -> folderDao.getFoldersWithAggregateExpenditureSortedByNameDesc()
            }

            SortCriteria.BY_CREATED -> when (sortOrder) {
                SortOrder.ASCENDING -> folderDao.getFoldersWithAggregateExpenditureSortedByCreatedAsc()
                SortOrder.DESCENDING -> folderDao.getFoldersWithAggregateExpenditureSortedByCreatedDesc()
            }
        }
    }
        .flow
        .map { it.map(FolderAndAggregateAmount::toTransactionFolderDetails) }

    override fun getFoldersListSortCriteria(): Flow<SortCriteria> = configDao
        .getFoldersListSortCriteria().map {
            SortCriteria.valueOf(
                it ?: SortCriteria.BY_NAME.name
            )
        }

    override fun getFoldersListSortOrder(): Flow<SortOrder> = configDao
        .getFoldersListSortOrder().map {
            SortOrder.valueOf(
                it ?: SortOrder.ASCENDING.name
            )
        }

    override suspend fun updateFoldersListSort(criteria: SortCriteria, order: SortOrder) {
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

    override fun getFoldersList(searchQuery: String): Flow<List<TransactionFolder>> =
        folderDao.getFoldersList(searchQuery)
            .map { it.map(FolderEntity::toTransactionFolder) }
}