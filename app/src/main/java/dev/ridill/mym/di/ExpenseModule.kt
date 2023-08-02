package dev.ridill.mym.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dev.ridill.mym.core.data.db.MYMDatabase
import dev.ridill.mym.core.domain.util.EventBus
import dev.ridill.mym.expense.data.local.ExpenseDao
import dev.ridill.mym.expense.data.local.TagsDao
import dev.ridill.mym.expense.data.repository.AddEditExpenseRepositoryImpl
import dev.ridill.mym.expense.domain.repository.AddEditExpenseRepository
import dev.ridill.mym.expense.presentation.addEditExpense.AddEditExpenseViewModel

@Module
@InstallIn(ViewModelComponent::class)
object ExpenseModule {

    @Provides
    fun provideExpenseDao(db: MYMDatabase): ExpenseDao = db.expenseDao()

    @Provides
    fun provideTagsDao(db: MYMDatabase): TagsDao = db.tagsDao()

    @Provides
    fun provideExpenseRepository(
        expenseDao: ExpenseDao,
        tagsDao: TagsDao
    ): AddEditExpenseRepository = AddEditExpenseRepositoryImpl(
        expenseDao = expenseDao,
        tagsDao = tagsDao
    )

    @Provides
    fun provideAddEditExpenseEventBus(): EventBus<AddEditExpenseViewModel.AddEditExpenseEvent> =
        EventBus()
}