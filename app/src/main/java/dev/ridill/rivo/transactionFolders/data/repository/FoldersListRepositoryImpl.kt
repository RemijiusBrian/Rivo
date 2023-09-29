package dev.ridill.rivo.transactionFolders.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import dev.ridill.rivo.core.domain.model.ListMode
import dev.ridill.rivo.core.domain.util.UtilConstants
import dev.ridill.rivo.settings.data.local.ConfigDao
import dev.ridill.rivo.settings.data.local.ConfigKeys
import dev.ridill.rivo.settings.data.local.entity.ConfigEntity
import dev.ridill.rivo.transactionFolders.data.local.TransactionFolderDao
import dev.ridill.rivo.transactionFolders.data.local.entity.TransactionFolderEntity
import dev.ridill.rivo.transactionFolders.data.local.relation.FolderAndAggregateAmount
import dev.ridill.rivo.transactionFolders.data.toTransactionFolder
import dev.ridill.rivo.transactionFolders.data.toTransactionFolderDetails
import dev.ridill.rivo.transactionFolders.domain.model.TransactionFolder
import dev.ridill.rivo.transactionFolders.domain.model.TransactionFolderDetails
import dev.ridill.rivo.transactionFolders.domain.repository.FoldersListRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class FoldersListRepositoryImpl(
    private val folderDao: TransactionFolderDao,
    private val configDao: ConfigDao
) : FoldersListRepository {
    override fun getFoldersWithAggregateList(): Flow<PagingData<TransactionFolderDetails>> =
        Pager(
            config = PagingConfig(pageSize = UtilConstants.DEFAULT_PAGE_SIZE)
        ) { folderDao.getFoldersWithAggregateExpenditure() }
            .flow
            .map { it.map(FolderAndAggregateAmount::toTransactionFolderDetails) }

    override fun getFoldersListMode(): Flow<ListMode> = configDao
        .getTansactionFoldersListMode().map {
            ListMode.valueOf(
                it ?: ListMode.GRID.name
            )
        }

    override suspend fun updateFoldersListMode(listMode: ListMode) {
        withContext(Dispatchers.IO) {
            configDao.insert(
                ConfigEntity(
                    configKey = ConfigKeys.TRANSACTION_FOLDERS_LIST_MODE,
                    configValue = listMode.name
                )
            )
        }
    }

    override fun getFoldersList(searchQuery: String): Flow<List<TransactionFolder>> =
        folderDao.getFoldersList(searchQuery)
            .map { it.map(TransactionFolderEntity::toTransactionFolder) }
}