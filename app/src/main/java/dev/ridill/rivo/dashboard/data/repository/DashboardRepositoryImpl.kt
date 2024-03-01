package dev.ridill.rivo.dashboard.data.repository

import android.icu.util.Currency
import dev.ridill.rivo.core.domain.util.DateUtil
import dev.ridill.rivo.dashboard.domain.repository.DashboardRepository
import dev.ridill.rivo.settings.domain.repositoty.BudgetRepository
import dev.ridill.rivo.settings.domain.repositoty.SettingsRepository
import dev.ridill.rivo.transactions.data.local.TransactionDao
import dev.ridill.rivo.transactions.data.local.views.TransactionDetailsView
import dev.ridill.rivo.transactions.data.toTransactionListItem
import dev.ridill.rivo.transactions.domain.model.TransactionListItem
import dev.ridill.rivo.transactions.domain.model.TransactionType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

class DashboardRepositoryImpl(
    private val transactionDao: TransactionDao,
    private val budgetRepository: BudgetRepository,
    private val settingsRepository: SettingsRepository
) : DashboardRepository {
    override fun getCurrencyPreference(): Flow<Currency> = settingsRepository
        .getCurrencyPreference()
        .distinctUntilChanged()

    override fun getCurrentBudget(): Flow<Long> = budgetRepository
        .getBudgetAmountForDateOrLatest()
        .distinctUntilChanged()

    override fun getExpenditureForCurrentMonth(): Flow<Double> = transactionDao.getAmountSum(
        typeName = TransactionType.DEBIT.name,
        dateTime = DateUtil.now()
    ).distinctUntilChanged()

    override fun getTotalCreditsForCurrentMonth(): Flow<Double> = transactionDao.getAmountSum(
        typeName = TransactionType.CREDIT.name,
        dateTime = DateUtil.now()
    ).distinctUntilChanged()

    override fun getRecentSpends(): Flow<List<TransactionListItem>> = transactionDao
        .getTransactionsList(
            monthAndYear = DateUtil.now(),
            transactionTypeName = TransactionType.DEBIT.name,
            showExcluded = false
        ).map { it.map(TransactionDetailsView::toTransactionListItem) }
}