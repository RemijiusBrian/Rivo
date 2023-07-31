package dev.ridill.mym.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dev.ridill.mym.core.data.db.MYMDatabase
import dev.ridill.mym.core.domain.util.EventBus
import dev.ridill.mym.expense.data.local.ExpenseDao
import dev.ridill.mym.expense.data.repository.ExpenseRepositoryImpl
import dev.ridill.mym.expense.domain.repository.ExpenseRepository
import dev.ridill.mym.expense.presentation.addEditExpense.AddEditExpenseViewModel

@Module
@InstallIn(ViewModelComponent::class)
object ExpenseModule {

    @Provides
    fun provideExpenseDao(db: MYMDatabase): ExpenseDao = db.expenseDao()

    @Provides
    fun provideExpenseRepository(
        dao: ExpenseDao
    ): ExpenseRepository = ExpenseRepositoryImpl(dao)

    @Provides
    fun provideExpenseEventBus(): EventBus<AddEditExpenseViewModel.AddEditExpenseEvent> = EventBus()
}