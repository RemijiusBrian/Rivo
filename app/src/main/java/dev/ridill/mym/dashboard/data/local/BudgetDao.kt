package dev.ridill.mym.dashboard.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import dev.ridill.mym.core.data.db.BaseDao
import dev.ridill.mym.dashboard.data.local.entity.BudgetEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BudgetDao : BaseDao<BudgetEntity> {
    @Query("SELECT * FROM BudgetEntity WHERE isCurrent = 1")
    fun getCurrentBudget(): Flow<BudgetEntity?>

    @Query("SELECT * FROM BudgetEntity WHERE isCurrent = 0 ORDER BY datetime(createdTimestamp) DESC LIMIT :limit")
    fun getPreviousBudgets(limit: Int): Flow<List<BudgetEntity>>

    @Query("UPDATE BudgetEntity SET isCurrent = (amount = :amount)")
    suspend fun updateCurrentBudget(amount: Long)

    @Transaction
    suspend fun insertAndSetCurrent(entity: BudgetEntity) {
        insert(entity)
        updateCurrentBudget(entity.amount)
    }
}