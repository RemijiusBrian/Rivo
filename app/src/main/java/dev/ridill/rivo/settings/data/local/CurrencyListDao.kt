package dev.ridill.rivo.settings.data.local

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Query
import dev.ridill.rivo.core.data.db.BaseDao
import dev.ridill.rivo.settings.data.local.entity.CurrencyListEntity

@Dao
interface CurrencyListDao : BaseDao<CurrencyListEntity> {
    @Query(
        """
        SELECT currency_code
        FROM currency_list_table
        WHERE (currency_code LIKE :query || '%')
        OR (display_name LIKE '%' || :query || '%')
    """
    )
    fun getAllCurrencyCodesPaged(query: String): PagingSource<Int, String>

    @Query(
        """
        SELECT NOT EXISTS(
            SELECT currency_code FROM currency_list_table LIMIT 1
        )
    """
    )
    suspend fun isTableEmpty(): Boolean
}