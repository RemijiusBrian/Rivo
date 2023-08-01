package dev.ridill.mym.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dev.ridill.mym.core.data.preferences.PreferencesManager
import dev.ridill.mym.dashboard.data.repository.DashboardRepositoryImpl
import dev.ridill.mym.dashboard.domain.repository.DashboardRepository
import dev.ridill.mym.expense.data.local.ExpenseDao

@Module
@InstallIn(ViewModelComponent::class)
object DashboardModule {

    @Provides
    fun provideDashboardRepository(
        dao: ExpenseDao,
        preferencesManager: PreferencesManager
    ): DashboardRepository = DashboardRepositoryImpl(dao, preferencesManager)
}