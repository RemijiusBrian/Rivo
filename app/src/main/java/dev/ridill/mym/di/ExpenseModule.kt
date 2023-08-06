package dev.ridill.mym.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dev.ridill.mym.core.data.db.MYMDatabase
import dev.ridill.mym.core.domain.util.EventBus
import dev.ridill.mym.expense.data.local.ExpenseDao
import dev.ridill.mym.expense.data.local.TagsDao
import dev.ridill.mym.expense.data.repository.ExpenseRepositoryImpl
import dev.ridill.mym.expense.data.repository.TagsRepositoryImpl
import dev.ridill.mym.expense.domain.notification.ExpenseNotificationHelper
import dev.ridill.mym.expense.domain.repository.ExpenseRepository
import dev.ridill.mym.expense.domain.repository.TagsRepository
import dev.ridill.mym.expense.domain.sms.ExpenseSmsService
import dev.ridill.mym.expense.presentation.addEditExpense.AddEditExpenseViewModel
import dev.ridill.mym.expense.presentation.allExpenses.AllExpensesViewModel

@Module
@InstallIn(SingletonComponent::class)
object ExpenseModule {

    @Provides
    fun provideExpenseDao(db: MYMDatabase): ExpenseDao = db.expenseDao()

    @Provides
    fun provideTagsDao(db: MYMDatabase): TagsDao = db.tagsDao()

    @Provides
    fun provideExpenseRepository(dao: ExpenseDao): ExpenseRepository = ExpenseRepositoryImpl(dao)

    @Provides
    fun provideTagsRepository(dao: TagsDao): TagsRepository = TagsRepositoryImpl(dao)

    @Provides
    fun provideAddEditExpenseEventBus(): EventBus<AddEditExpenseViewModel.AddEditExpenseEvent> =
        EventBus()

    @Provides
    fun provideAllExpenseEventBus(): EventBus<AllExpensesViewModel.AllExpenseEvent> = EventBus()

    @Provides
    fun provideExpenseSmsService(): ExpenseSmsService = ExpenseSmsService()

    @Provides
    fun provideExpenseNotificationHelper(
        @ApplicationContext context: Context
    ): ExpenseNotificationHelper = ExpenseNotificationHelper(context)
}