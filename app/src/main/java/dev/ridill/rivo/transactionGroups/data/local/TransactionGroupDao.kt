package dev.ridill.rivo.transactionGroups.data.local

import androidx.room.Dao
import androidx.room.Query
import dev.ridill.rivo.core.data.db.BaseDao
import dev.ridill.rivo.transactionGroups.data.local.entity.TransactionGroupEntity
import dev.ridill.rivo.transactionGroups.data.local.relation.GroupAndTransactions
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionGroupDao : BaseDao<TransactionGroupEntity> {

    @Query("SELECT * FROM transaction_group_table ORDER BY datetime(created_timestamp) DESC, id DESC")
    fun getGroups(): Flow<List<TransactionGroupEntity>>

    @Query("SELECT * FROM transaction_group_table WHERE id = :id")
    fun getGroupWithTransactionsById(id: Long): Flow<GroupAndTransactions>
}