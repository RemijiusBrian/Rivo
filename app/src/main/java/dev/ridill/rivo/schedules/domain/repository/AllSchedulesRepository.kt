package dev.ridill.rivo.schedules.domain.repository

import androidx.paging.PagingData
import dev.ridill.rivo.core.domain.model.Resource
import dev.ridill.rivo.core.domain.util.DateUtil
import dev.ridill.rivo.schedules.domain.model.ScheduleListItemUiModel
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface AllSchedulesRepository {
    fun getAllSchedules(
        dateNow: LocalDate = DateUtil.dateNow()
    ): Flow<PagingData<ScheduleListItemUiModel>>

    suspend fun markScheduleAsPaid(id: Long): Resource<Unit>
    suspend fun deleteSchedulesById(ids: Set<Long>)
}