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
import dev.ridill.rivo.schedules.data.toSchedule
import dev.ridill.rivo.schedules.data.toScheduleListItem
import dev.ridill.rivo.schedules.data.toTransaction
import dev.ridill.rivo.schedules.domain.model.ScheduleListItemUiModel
import dev.ridill.rivo.schedules.domain.model.ScheduleRepeatMode
import dev.ridill.rivo.schedules.domain.repository.SchedulesAndPlansRepository
import dev.ridill.rivo.transactions.domain.repository.AddEditTransactionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.time.LocalDate

class SchedulesAndPlansRepositoryImpl(
    private val db: RivoDatabase,
    private val dao: SchedulesDao,
    private val transactionRepository: AddEditTransactionRepository
) : SchedulesAndPlansRepository {
    override fun getSchedules(dateNow: LocalDate): Flow<PagingData<ScheduleListItemUiModel>> =
        Pager(
            config = PagingConfig(UtilConstants.DEFAULT_PAGE_SIZE)
        ) {
            dao.getAllSchedulesWithLastTransaction()
        }.flow
            .map { pagingData ->
                pagingData.map { it.toScheduleListItem(dateNow) }
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
                                    && after?.scheduleItem?.nextReminderDate == null
                            -> ScheduleListItemUiModel.TypeSeparator(UiText.StringResource(R.string.retired))

                            else -> null
                        }
                    }
            }

    override suspend fun markScheduleAsPaid(id: Long): Resource<Unit> =
        withContext(Dispatchers.IO) {
            try {
                db.withTransaction {
                    val schedule = dao.getScheduleById(id)
                        ?: throw ScheduleNotFoundThrowable()
                    val tx = schedule.toSchedule().toTransaction()
                    transactionRepository.saveTransaction(tx)
                    val nextReminderDate = schedule.nextReminderDate?.let {
                        when (ScheduleRepeatMode.valueOf(schedule.repeatModeName)) {
                            ScheduleRepeatMode.NO_REPEAT -> null
                            ScheduleRepeatMode.WEEKLY -> it.plusWeeks(1)
                            ScheduleRepeatMode.MONTHLY -> it.plusMonths(1)
                            ScheduleRepeatMode.BI_MONTHLY -> it.plusMonths(2)
                            ScheduleRepeatMode.YEARLY -> it.plusYears(1)
                        }
                    }
                    dao.updateNextReminderDateForScheduleById(id, nextReminderDate)
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
}

class ScheduleNotFoundThrowable : Throwable("Schedule not found")