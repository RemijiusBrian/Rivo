package dev.ridill.rivo.transactionGroups.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import dev.ridill.rivo.core.data.db.BaseDao
import dev.ridill.rivo.transactionGroups.data.local.entity.TransactionGroupEntity
import dev.ridill.rivo.transactionGroups.data.local.relation.GroupAndAggregateAmount
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionGroupDao : BaseDao<TransactionGroupEntity> {

    @Transaction
    @Query(
        """
        SELECT txGroup.id, txGroup.name, txGroup.created_timestamp AS createdTimestamp, txGroup.is_excluded as excluded,
        ((SELECT IFNULL(SUM(tx1.amount), 0.0) FROM transaction_table tx1 WHERE tx1.group_id = txGroup.id AND tx1.transaction_direction = 'OUTGOING')
        - (SELECT IFNULL(SUM(tx2.amount), 0.0) FROM transaction_table tx2 WHERE tx2.group_id = txGroup.id AND tx2.transaction_direction = 'INCOMING')
        ) AS aggregateAmount
        FROM transaction_group_table txGroup
        ORDER BY datetime(createdTimestamp) DESC, id DESC
    """
    )
    fun getGroupsWithAggregateExpenditure(): Flow<List<GroupAndAggregateAmount>>

    @Transaction
    @Query(
        """
        SELECT txGroup.id, txGroup.name, txGroup.created_timestamp AS createdTimestamp, txGroup.is_excluded as excluded,
        ((SELECT IFNULL(SUM(tx1.amount), 0.0) FROM transaction_table tx1 WHERE tx1.group_id = txGroup.id AND tx1.transaction_direction = 'OUTGOING')
        - (SELECT IFNULL(SUM(tx2.amount), 0.0) FROM transaction_table tx2 WHERE tx2.group_id = txGroup.id AND tx2.transaction_direction = 'INCOMING')
        ) AS aggregateAmount
        FROM transaction_group_table txGroup
        WHERE txGroup.id = :id
        ORDER BY datetime(createdTimestamp) DESC, id DESC
    """
    )
    fun getGroupDetailsWithAggregateExpenditureById(id: Long): Flow<GroupAndAggregateAmount?>
}