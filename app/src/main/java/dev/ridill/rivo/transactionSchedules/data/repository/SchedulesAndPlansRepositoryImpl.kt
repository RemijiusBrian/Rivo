package dev.ridill.rivo.transactionSchedules.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.insertSeparators
import androidx.paging.map
import dev.ridill.rivo.R
import dev.ridill.rivo.core.domain.model.Resource
import dev.ridill.rivo.core.domain.util.UtilConstants
import dev.ridill.rivo.core.domain.util.logD
import dev.ridill.rivo.core.ui.util.UiText
import dev.ridill.rivo.transactionSchedules.data.local.TxSchedulesDao
import dev.ridill.rivo.transactionSchedules.data.toListItem
import dev.ridill.rivo.transactionSchedules.data.toSchedule
import dev.ridill.rivo.transactionSchedules.data.toTransaction
import dev.ridill.rivo.transactionSchedules.domain.model.ScheduleListItemUiModel
import dev.ridill.rivo.transactionSchedules.domain.model.ScheduleRepeatMode
import dev.ridill.rivo.transactionSchedules.domain.model.TxScheduleStatus
import dev.ridill.rivo.transactionSchedules.domain.repository.SchedulesAndPlansRepository
import dev.ridill.rivo.transactions.domain.repository.AddEditTransactionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.time.LocalDate

class SchedulesAndPlansRepositoryImpl(
    private val dao: TxSchedulesDao,
    private val transactionRepository: AddEditTransactionRepository
) : SchedulesAndPlansRepository {
    override fun getSchedules(date: LocalDate): Flow<PagingData<ScheduleListItemUiModel>> = Pager(
        config = PagingConfig(UtilConstants.DEFAULT_PAGE_SIZE)
    ) {
        dao.getAllSchedulesWithLastPaidDates()
    }.flow
        .map { pagingData ->
            pagingData.map {
                val item = it.toListItem(date)
                logD { "$item" }
                item
            }
        }
        .map { pagingData -> pagingData.map { ScheduleListItemUiModel.ScheduleItem(it) } }
        .map { pagingData ->
            pagingData
                .insertSeparators<ScheduleListItemUiModel.ScheduleItem, ScheduleListItemUiModel>
                { before, after ->
                    when {
                        before?.scheduleItem?.status != TxScheduleStatus.RETIRED
                                && after?.scheduleItem?.status == TxScheduleStatus.RETIRED ->
                            ScheduleListItemUiModel.RetiredSeparator

                        before?.scheduleItem?.nextReminderDate != after?.scheduleItem?.nextReminderDate ->
                            after?.scheduleItem?.nextReminderDate?.let {
                                ScheduleListItemUiModel.DateSeparator(it)
                            }

                        else -> null
                    }
                }
        }

    override suspend fun markScheduleAsPaid(id: Long): Resource<Unit> =
        withContext(Dispatchers.IO) {
            try {
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