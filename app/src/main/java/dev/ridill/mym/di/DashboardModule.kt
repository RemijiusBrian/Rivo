package dev.ridill.mym.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dev.ridill.mym.dashboard.data.repository.DashboardRepositoryImpl
import dev.ridill.mym.dashboard.domain.repository.DashboardRepository
import dev.ridill.mym.expense.data.local.TransactionDao
import dev.ridill.mym.settings.domain.repositoty.SettingsRepository

@Module
@InstallIn(ViewModelComponent::class)
object DashboardModule {
    @Provides
    fun provideDashboardRepository(
        transactionDao: TransactionDao,
        settingsRepository: SettingsRepository
    ): DashboardRepository = DashboardRepositoryImpl(
        transactionDao = transactionDao,
        settingsRepository = settingsRepository
    )
}