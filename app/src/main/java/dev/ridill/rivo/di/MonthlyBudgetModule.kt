package dev.ridill.rivo.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dev.ridill.rivo.core.data.db.RivoDatabase
import dev.ridill.rivo.core.domain.util.EventBus
import dev.ridill.rivo.settings.data.local.BudgetPreferenceDao
import dev.ridill.rivo.settings.data.repository.BudgetPreferenceRepositoryImpl
import dev.ridill.rivo.settings.domain.repositoty.BudgetPreferenceRepository
import dev.ridill.rivo.settings.presentation.budgetUpdate.UpdateBudgetViewModel

@Module
@InstallIn(ViewModelComponent::class)
object MonthlyBudgetModule {

    @Provides
    fun provideBudgetPreferenceDao(database: RivoDatabase): BudgetPreferenceDao =
        database.budgetPreferenceDao()

    @Provides
    fun provideBudgetPreferenceRepository(
        dao: BudgetPreferenceDao
    ): BudgetPreferenceRepository = BudgetPreferenceRepositoryImpl(dao)

    @Provides
    fun provideUpdateBudgetEventBus(): EventBus<UpdateBudgetViewModel.UpdateBudgetEvent> =
        EventBus()
}