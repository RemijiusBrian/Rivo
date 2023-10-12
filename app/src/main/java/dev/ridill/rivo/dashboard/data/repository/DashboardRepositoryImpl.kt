package dev.ridill.rivo.dashboard.data.repository

import android.icu.util.Currency
import dev.ridill.rivo.core.domain.util.DateUtil
import dev.ridill.rivo.dashboard.domain.repository.DashboardRepository
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
    private val settingsRepository: SettingsRepository
) : DashboardRepository {
    override fun getCurrencyPreference(): Flow<Currency> = settingsRepository
        .getCurrencyPreference()
        .distinctUntilChanged()

    override fun getCurrentBudget(): Flow<Long> = settingsRepository.getCurrentBudget()
        .distinctUntilChanged()

    override fun getExpenditureForCurrentMonth(): Flow<Double> =
        transactionDao.getAmountSum(
            monthAndYear = DateUtil.now(),
            typeName = TransactionType.DEBIT.name
        )
            .distinctUntilChanged()

    override fun getRecentSpends(): Flow<List<TransactionListItem>> = transactionDao
        .getTransactionsList(
            monthAndYear = DateUtil.now(),
            transactionTypeName = TransactionType.DEBIT.name,
            showExcluded = false
        ).map { it.map(TransactionDetailsView::toTransactionListItem) }
}