package dev.ridill.rivo.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dev.ridill.rivo.dashboard.data.repository.DashboardRepositoryImpl
import dev.ridill.rivo.dashboard.domain.repository.DashboardRepository
import dev.ridill.rivo.settings.domain.repositoty.BudgetRepository
import dev.ridill.rivo.settings.domain.repositoty.SettingsRepository
import dev.ridill.rivo.transactions.data.local.TransactionDao

@Module
@InstallIn(ViewModelComponent::class)
object DashboardModule {
    @Provides
    fun provideDashboardRepository(
        transactionDao: TransactionDao,
        budgetRepository: BudgetRepository,
        settingsRepository: SettingsRepository
    ): DashboardRepository = DashboardRepositoryImpl(
        transactionDao = transactionDao,
        budgetRepository = budgetRepository,
        settingsRepository = settingsRepository
    )
}