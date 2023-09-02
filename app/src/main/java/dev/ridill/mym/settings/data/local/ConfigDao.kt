package dev.ridill.mym.settings.data.local

import androidx.room.Dao
import androidx.room.Query
import dev.ridill.mym.core.data.db.BaseDao
import dev.ridill.mym.settings.data.local.entity.ConfigEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ConfigDao : BaseDao<ConfigEntity> {
    @Query("SELECT IFNULL(configValue, 0) FROM ConfigEntity WHERE configKey = '${ConfigKeys.BUDGET_AMOUNT}'")
    fun getBudgetAmount(): Flow<Long?>

    @Query("SELECT configValue FROM ConfigEntity WHERE configKey = '${ConfigKeys.CURRENCY_CODE}'")
    fun getCurrencyCode(): Flow<String?>

    @Query("SELECT configValue FROM ConfigEntity WHERE configKey = '${ConfigKeys.BACKUP_INTERVAL}'")
    suspend fun getBackupInterval(): String?
}