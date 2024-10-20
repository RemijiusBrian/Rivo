package dev.ridill.rivo.settings.data.local

import androidx.room.Dao
import androidx.room.Query
import dev.ridill.rivo.core.data.db.BaseDao
import dev.ridill.rivo.settings.data.local.entity.CurrencyPreferenceEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface CurrencyPreferenceDao : BaseDao<CurrencyPreferenceEntity> {
    @Query(
        """
        SELECT currency_code
        FROM currency_preference_table
        WHERE DATE(date) <= DATE(:date)
        ORDER BY DATE(date) DESC
        LIMIT 1
    """
    )
    fun getCurrencyCodeForDateOrLast(date: LocalDate): Flow<String?>
}