package dev.ridill.rivo.schedules.domain.repository

import androidx.paging.PagingData
import dev.ridill.rivo.core.domain.model.Resource
import dev.ridill.rivo.core.domain.util.DateUtil
import dev.ridill.rivo.schedules.domain.model.PlanInput
import dev.ridill.rivo.schedules.domain.model.PlanListItem
import dev.ridill.rivo.schedules.domain.model.ScheduleListItemUiModel
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface SchedulesDashboardRepository {
    fun getPlansPaged(date: LocalDate? = null): Flow<PagingData<PlanListItem>>
    fun getSchedules(
        dateNow: LocalDate = DateUtil.dateNow()
    ): Flow<PagingData<ScheduleListItemUiModel>>

    suspend fun markScheduleAsPaid(id: Long): Resource<Unit>
    suspend fun savePlan(input: PlanInput)
    suspend fun deletePlan(plan: PlanInput)
    suspend fun assignSchedulesToPlan(scheduleIds: Set<Long>, planId: Long?)
    suspend fun deleteSchedulesById(ids: Set<Long>)
}