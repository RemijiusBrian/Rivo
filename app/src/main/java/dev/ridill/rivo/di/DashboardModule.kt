package dev.ridill.rivo.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dev.ridill.rivo.core.domain.util.EventBus
import dev.ridill.rivo.dashboard.data.repository.DashboardRepositoryImpl
import dev.ridill.rivo.dashboard.domain.repository.DashboardRepository
import dev.ridill.rivo.dashboard.presentation.DashboardViewModel
import dev.ridill.rivo.schedules.data.local.SchedulesDao
import dev.ridill.rivo.settings.domain.repositoty.BudgetRepository
import dev.ridill.rivo.settings.domain.repositoty.CurrencyRepository
import dev.ridill.rivo.transactions.data.local.TransactionDao

@Module
@InstallIn(ViewModelComponent::class)
object DashboardModule {
    @Provides
    fun provideDashboardRepository(
        currencyRepository: CurrencyRepository,
        budgetRepository: BudgetRepository,
        transactionDao: TransactionDao,
        schedulesDao: SchedulesDao
    ): DashboardRepository = DashboardRepositoryImpl(
        currencyRepo = currencyRepository,
        budgetRepo = budgetRepository,
        transactionDao = transactionDao,
        schedulesDao = schedulesDao
    )

    @Provides
    fun provideDashboardEventBus(): EventBus<DashboardViewModel.DashboardEvent> = EventBus()
}