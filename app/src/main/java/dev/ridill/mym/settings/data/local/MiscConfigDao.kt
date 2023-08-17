package dev.ridill.mym.settings.data.local

import androidx.room.Dao
import androidx.room.Query
import dev.ridill.mym.core.data.db.BaseDao
import dev.ridill.mym.settings.data.local.entity.MiscConfigEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MiscConfigDao : BaseDao<MiscConfigEntity> {
    @Query("SELECT IFNULL(configValue, 0) FROM MiscConfigEntity WHERE configKey = '${ConfigKeys.BUDGET_AMOUNT}'")
    fun getBudgetAmount(): Flow<Long?>

    @Query("SELECT configValue FROM MiscConfigEntity WHERE configKey = '${ConfigKeys.BACKUP_INTERVAL}'")
    suspend fun getBackupInterval(): String?
}