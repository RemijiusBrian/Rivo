package dev.ridill.rivo.dashboard.data.repository

import dev.ridill.rivo.core.domain.util.DateUtil
import dev.ridill.rivo.dashboard.domain.repository.DashboardRepository
import dev.ridill.rivo.schedules.data.local.SchedulesDao
import dev.ridill.rivo.schedules.data.local.entity.ScheduleEntity
import dev.ridill.rivo.schedules.data.toActiveSchedule
import dev.ridill.rivo.schedules.domain.model.UpcomingSchedule
import dev.ridill.rivo.settings.domain.repositoty.BudgetRepository
import dev.ridill.rivo.settings.domain.repositoty.CurrencyRepository
import dev.ridill.rivo.transactions.data.local.TransactionDao
import dev.ridill.rivo.transactions.data.local.views.TransactionDetailsView
import dev.ridill.rivo.transactions.data.toTransactionListItem
import dev.ridill.rivo.transactions.domain.model.TransactionListItem
import dev.ridill.rivo.transactions.domain.model.TransactionType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import java.util.Currency

class DashboardRepositoryImpl(
    private val currencyRepo: CurrencyRepository,
    private val budgetRepo: BudgetRepository,
    private val transactionDao: TransactionDao,
    private val schedulesDao: SchedulesDao
) : DashboardRepository {
    override fun getCurrencyPreference(): Flow<Currency> = currencyRepo
        .getCurrencyForDateOrNext()
        .distinctUntilChanged()

    override fun getCurrentBudget(): Flow<Long> = budgetRepo
        .getBudgetAmountForDateOrNext()
        .distinctUntilChanged()

    override fun getExpenditureForCurrentMonth(): Flow<Double> = transactionDao.getAmountSum(
        typeName = TransactionType.DEBIT.name,
        dateTime = DateUtil.now()
    ).distinctUntilChanged()

    override fun getTotalCreditsForCurrentMonth(): Flow<Double> = transactionDao.getAmountSum(
        typeName = TransactionType.CREDIT.name,
        dateTime = DateUtil.now()
    ).distinctUntilChanged()

    override fun getActiveSchedules(): Flow<List<UpcomingSchedule>> = schedulesDao
        .getUpcomingSchedulesForDate(DateUtil.dateNow())
        .map { entities -> entities.map(ScheduleEntity::toActiveSchedule) }

    override fun getRecentSpends(): Flow<List<TransactionListItem>> = transactionDao
        .getTransactionsList(
            monthAndYear = DateUtil.now(),
            transactionTypeName = TransactionType.DEBIT.name,
            showExcluded = false
        ).map { it.map(TransactionDetailsView::toTransactionListItem) }
}