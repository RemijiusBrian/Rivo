package dev.ridill.rivo.transactionSchedules.domain.repository

import androidx.paging.PagingData
import dev.ridill.rivo.core.domain.model.Resource
import dev.ridill.rivo.core.domain.util.DateUtil
import dev.ridill.rivo.transactionSchedules.domain.model.ScheduleListItemUiModel
import dev.ridill.rivo.transactionSchedules.domain.model.TxScheduleListItem
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface SchedulesAndPlansRepository {
    fun getSchedules(
        date: LocalDate = DateUtil.dateNow()
    ): Flow<PagingData<ScheduleListItemUiModel>>

    suspend fun markScheduleAsPaid(id: Long): Resource<Unit>
}