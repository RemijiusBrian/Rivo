package dev.ridill.rivo.dashboard.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import dev.ridill.rivo.core.domain.util.DateUtil
import dev.ridill.rivo.core.domain.util.UtilConstants
import dev.ridill.rivo.dashboard.domain.repository.DashboardRepository
import dev.ridill.rivo.schedules.data.local.SchedulesDao
import dev.ridill.rivo.schedules.data.local.entity.ScheduleEntity
import dev.ridill.rivo.schedules.data.toActiveSchedule
import dev.ridill.rivo.schedules.domain.model.ActiveSchedule
import dev.ridill.rivo.settings.domain.repositoty.BudgetPreferenceRepository
import dev.ridill.rivo.transactions.data.local.TransactionDao
import dev.ridill.rivo.transactions.data.local.views.TransactionDetailsView
import dev.ridill.rivo.transactions.data.toTransactionListItem
import dev.ridill.rivo.transactions.domain.model.TransactionListItem
import dev.ridill.rivo.transactions.domain.model.TransactionType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

class DashboardRepositoryImpl(
    private val budgetPrefRepo: BudgetPreferenceRepository,
    private val transactionDao: TransactionDao,
    private val schedulesDao: SchedulesDao
) : DashboardRepository {
    override fun getCurrentBudget(): Flow<Long> = budgetPrefRepo
        .getBudgetPreferenceForDateOrNext()
        .distinctUntilChanged()

    override fun getExpenditureForCurrentMonth(): Flow<Double> = transactionDao.getAmountAggregate(
        startDate = LocalDate.now().withDayOfMonth(1),
        endDate = LocalDate.now().with(TemporalAdjusters.lastDayOfMonth()),
        type = TransactionType.DEBIT,
        tagIds = null,
        addExcluded = false,
        selectedTxIds = null
    ).distinctUntilChanged()

    override fun getTotalCreditsForCurrentMonth(): Flow<Double> = transactionDao.getAmountAggregate(
        startDate = LocalDate.now().withDayOfMonth(1),
        endDate = LocalDate.now().with(TemporalAdjusters.lastDayOfMonth()),
        type = TransactionType.CREDIT,
        tagIds = null,
        addExcluded = false,
        selectedTxIds = null
    ).distinctUntilChanged()

    override fun getSchedulesActiveThisMonth(): Flow<List<ActiveSchedule>> = schedulesDao
        .getSchedulesForMonth(DateUtil.dateNow())
        .map { entities -> entities.map(ScheduleEntity::toActiveSchedule) }

    override fun getRecentSpends(): Flow<PagingData<TransactionListItem>> = Pager(
        config = PagingConfig(UtilConstants.DEFAULT_PAGE_SIZE)
    ) {
        transactionDao
            .getTransactionsPaged(
                startDate = LocalDate.now().withDayOfMonth(1),
                endDate = LocalDate.now().with(TemporalAdjusters.lastDayOfMonth()),
                type = TransactionType.DEBIT,
                showExcluded = false,
                tagIds = null,
                folderId = null
            )
    }.flow
        .map { it.map(TransactionDetailsView::toTransactionListItem) }
}