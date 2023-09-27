package dev.ridill.rivo.transactionGroups.domain.repository

import dev.ridill.rivo.core.domain.model.ListMode
import dev.ridill.rivo.transactionGroups.domain.model.TxGroupDetails
import kotlinx.coroutines.flow.Flow

interface TxGroupsListRepository {
    fun getGroupsList(): Flow<List<TxGroupDetails>>
    fun getGroupsListMode(): Flow<ListMode>
    suspend fun updateGroupsListMode(listMode: ListMode)
}