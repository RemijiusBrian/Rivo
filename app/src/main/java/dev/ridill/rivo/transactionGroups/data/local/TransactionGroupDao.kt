package dev.ridill.rivo.transactionGroups.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import dev.ridill.rivo.core.data.db.BaseDao
import dev.ridill.rivo.transactionGroups.data.local.entity.TransactionGroupEntity
import dev.ridill.rivo.transactionGroups.data.local.relation.GroupAndAggregateAmount
import dev.ridill.rivo.transactionGroups.data.local.relation.GroupAndTransactions
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionGroupDao : BaseDao<TransactionGroupEntity> {

    @Transaction
    @Query(
        """
        SELECT id, name, created_timestamp AS createdTimestamp,
        IFNULL(((SELECT SUM(amount) FROM transaction_table WHERE group_id = id AND transaction_direction = 'OUTGOING')
        - (SELECT SUM(amount) FROM transaction_table WHERE group_id = id AND transaction_direction = 'INCOMING')
        ), 0.0) AS aggregateAmount
        FROM transaction_group_table
        ORDER BY datetime(created_timestamp) DESC, id DESC
    """
    )
    fun getGroupsWithAggregateExpenditure(): Flow<List<GroupAndAggregateAmount>>

    @Transaction
    @Query("SELECT * FROM transaction_group_table WHERE id = :id")
    fun getGroupWithTransactionsById(id: Long): Flow<GroupAndTransactions>
}