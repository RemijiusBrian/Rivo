package dev.ridill.rivo.transactionGroups.data.repository

import dev.ridill.rivo.transactionGroups.data.local.TransactionGroupDao
import dev.ridill.rivo.transactionGroups.data.local.relation.GroupAndAggregateAmount
import dev.ridill.rivo.transactionGroups.data.toTxGroupListItem
import dev.ridill.rivo.transactionGroups.domain.model.TxGroupListItem
import dev.ridill.rivo.transactionGroups.domain.repository.TxGroupsListRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class TxGroupsListRepositoryImpl(
    private val dao: TransactionGroupDao
) : TxGroupsListRepository {
    override fun getGroupsList(): Flow<List<TxGroupListItem>> =
        dao.getGroupsWithAggregateExpenditure()
            .map { entities ->
                entities.map(GroupAndAggregateAmount::toTxGroupListItem)
            }
}