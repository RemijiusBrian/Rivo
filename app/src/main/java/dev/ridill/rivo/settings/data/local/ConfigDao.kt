package dev.ridill.rivo.settings.data.local

import androidx.room.Dao
import androidx.room.Query
import dev.ridill.rivo.core.data.db.BaseDao
import dev.ridill.rivo.settings.data.local.entity.ConfigEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ConfigDao : BaseDao<ConfigEntity> {
    @Query("SELECT IFNULL(config_value, 0) FROM config_table WHERE config_key = '${ConfigKeys.BUDGET_AMOUNT}'")
    fun getBudgetAmount(): Flow<Long?>

    @Query("SELECT config_value FROM config_table WHERE config_key = '${ConfigKeys.CURRENCY_CODE}'")
    fun getCurrencyCode(): Flow<String?>

    @Query("SELECT config_value FROM config_table WHERE config_key = '${ConfigKeys.BACKUP_INTERVAL}'")
    suspend fun getBackupInterval(): String?

    @Query("SELECT config_value FROM config_table WHERE config_key = '${ConfigKeys.FOLDERS_LIST_SORT_CRITERIA}'")
    fun getFoldersListSortCriteria(): Flow<String?>

    @Query("SELECT config_value FROM config_table WHERE config_key = '${ConfigKeys.FOLDERS_LIST_SORT_ORDER}'")
    fun getFoldersListSortOrder(): Flow<String?>

    @Query("SELECT config_value FROM config_table WHERE config_key = '${ConfigKeys.FOLDERS_LIST_MODE}'")
    fun getFoldersListMode(): Flow<String?>
}