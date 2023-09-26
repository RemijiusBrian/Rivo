package dev.ridill.rivo.transactionGroups.domain.repository

import dev.ridill.rivo.transactionGroups.domain.model.TxGroupListItem
import kotlinx.coroutines.flow.Flow

interface TxGroupsListRepository {
    fun getGroupsList(): Flow<List<TxGroupListItem>>
}