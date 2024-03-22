package dev.ridill.rivo.schedules.data.local

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import dev.ridill.rivo.core.data.db.BaseDao
import dev.ridill.rivo.schedules.data.local.entity.SchedulePlanEntity
import dev.ridill.rivo.schedules.data.local.views.PlanAndAmountsView

@Dao
interface PlansDao : BaseDao<SchedulePlanEntity> {
    @Transaction
    @Query(
        """
            SELECT *
            FROM plan_and_amounts_view
    """
    )
    fun getPlansActiveOnDatePaged(): PagingSource<Int, PlanAndAmountsView>
}