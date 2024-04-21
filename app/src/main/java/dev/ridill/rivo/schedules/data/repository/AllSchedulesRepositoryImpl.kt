package dev.ridill.rivo.schedules.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.insertSeparators
import androidx.paging.map
import androidx.room.withTransaction
import dev.ridill.rivo.R
import dev.ridill.rivo.core.data.db.RivoDatabase
import dev.ridill.rivo.core.domain.model.Resource
import dev.ridill.rivo.core.domain.util.UtilConstants
import dev.ridill.rivo.core.domain.util.orZero
import dev.ridill.rivo.core.ui.util.UiText
import dev.ridill.rivo.schedules.data.local.SchedulesDao
import dev.ridill.rivo.schedules.data.toScheduleListItem
import dev.ridill.rivo.schedules.domain.model.ScheduleListItemUiModel
import dev.ridill.rivo.schedules.domain.repository.AllSchedulesRepository
import dev.ridill.rivo.schedules.domain.repository.SchedulesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.time.LocalDate

class AllSchedulesRepositoryImpl(
    private val db: RivoDatabase,
    private val schedulesDao: SchedulesDao,
    private val repo: SchedulesRepository
) : AllSchedulesRepository {
    override fun getAllSchedules(dateNow: LocalDate): Flow<PagingData<ScheduleListItemUiModel>> =
        Pager(PagingConfig(UtilConstants.DEFAULT_PAGE_SIZE)) {
            schedulesDao.getAllSchedulesPaged()
        }.flow
            .map { pagingData ->
                pagingData.map { it.toScheduleListItem() }
            }
            .map { pagingData -> pagingData.map { ScheduleListItemUiModel.ScheduleItem(it) } }
            .map { pagingData ->
                pagingData
                    .insertSeparators<ScheduleListItemUiModel.ScheduleItem, ScheduleListItemUiModel>
                    { before, after ->
                        when {
                            before?.scheduleItem?.nextReminderDate?.monthValue != dateNow.monthValue
                                    && after?.scheduleItem?.nextReminderDate?.monthValue == dateNow.monthValue
                            -> ScheduleListItemUiModel.TypeSeparator(UiText.StringResource(R.string.this_month))

                            before?.scheduleItem?.nextReminderDate?.monthValue.orZero() <= dateNow.monthValue
                                    && after?.scheduleItem?.nextReminderDate?.monthValue.orZero() > dateNow.monthValue
                            -> ScheduleListItemUiModel.TypeSeparator(UiText.StringResource(R.string.upcoming))

                            before?.scheduleItem?.nextReminderDate != null
                                    && after?.scheduleItem != null
                                    && after.scheduleItem.nextReminderDate == null
                            -> ScheduleListItemUiModel.TypeSeparator(UiText.StringResource(R.string.retired))

                            else -> null
                        }
                    }
            }

    override suspend fun markScheduleAsPaid(id: Long): Resource<Unit> =
        withContext(Dispatchers.IO) {
            try {
                db.withTransaction {
                    val schedule = repo.getScheduleById(id)
                        ?: throw ScheduleNotFoundThrowable()
                    repo.createTransactionForScheduleAndSetNextReminder(schedule)
                    Resource.Success(Unit)
                }
            } catch (t: ScheduleNotFoundThrowable) {
                Resource.Error(UiText.StringResource(R.string.error_schedule_not_found))
            } catch (t: Throwable) {
                Resource.Error(
                    t.localizedMessage?.let {
                        UiText.DynamicString(it)
                    } ?: UiText.StringResource(R.string.error_unknown)
                )
            }
        }

    override suspend fun deleteSchedulesById(ids: Set<Long>) = withContext(Dispatchers.IO) {
        repo.deleteSchedulesByIds(ids)
    }
}

class ScheduleNotFoundThrowable : Throwable("Schedule not found")