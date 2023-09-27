package dev.ridill.rivo.transactionGroups.data.repository

import dev.ridill.rivo.core.domain.model.ListMode
import dev.ridill.rivo.settings.data.local.ConfigDao
import dev.ridill.rivo.settings.data.local.ConfigKeys
import dev.ridill.rivo.settings.data.local.entity.ConfigEntity
import dev.ridill.rivo.transactionGroups.data.local.TransactionGroupDao
import dev.ridill.rivo.transactionGroups.data.local.relation.GroupAndAggregateAmount
import dev.ridill.rivo.transactionGroups.data.toTxGroupDetails
import dev.ridill.rivo.transactionGroups.domain.model.TxGroupDetails
import dev.ridill.rivo.transactionGroups.domain.repository.TxGroupsListRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class TxGroupsListRepositoryImpl(
    private val transactionGroupDao: TransactionGroupDao,
    private val configDao: ConfigDao
) : TxGroupsListRepository {
    override fun getGroupsList(): Flow<List<TxGroupDetails>> =
        transactionGroupDao.getGroupsWithAggregateExpenditure()
            .map { entities ->
                entities.map(GroupAndAggregateAmount::toTxGroupDetails)
            }

    override fun getGroupsListMode(): Flow<ListMode> = configDao
        .getTxGroupsListMode().map {
            ListMode.valueOf(
                it ?: ListMode.GRID.name
            )
        }

    override suspend fun updateGroupsListMode(listMode: ListMode) {
        withContext(Dispatchers.IO) {
            configDao.insert(
                ConfigEntity(
                    configKey = ConfigKeys.TX_GROUPS_LIST_MODE,
                    configValue = listMode.name
                )
            )
        }
    }
}