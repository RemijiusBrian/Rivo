package dev.ridill.mym.settings.data.local

import androidx.room.Dao
import androidx.room.Query
import dev.ridill.mym.core.data.db.BaseDao
import dev.ridill.mym.settings.data.local.entity.ConfigEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ConfigDao : BaseDao<ConfigEntity> {
    @Query("SELECT IFNULL(config_value, 0) FROM config_table WHERE config_key = '${ConfigKeys.BUDGET_AMOUNT}'")
    fun getBudgetAmount(): Flow<Long?>

    @Query("SELECT config_value FROM config_table WHERE config_key = '${ConfigKeys.CURRENCY_CODE}'")
    fun getCurrencyCode(): Flow<String?>

    @Query("SELECT config_value FROM config_table WHERE config_key = '${ConfigKeys.BACKUP_INTERVAL}'")
    suspend fun getBackupInterval(): String?
}