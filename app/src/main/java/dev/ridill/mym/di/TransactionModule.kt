package dev.ridill.mym.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dev.ridill.mym.core.data.db.RivoDatabase
import dev.ridill.mym.core.data.preferences.PreferencesManager
import dev.ridill.mym.core.domain.util.EventBus
import dev.ridill.mym.expense.data.local.TagsDao
import dev.ridill.mym.expense.data.local.TransactionDao
import dev.ridill.mym.expense.data.repository.ExpenseRepositoryImpl
import dev.ridill.mym.expense.data.repository.TagsRepositoryImpl
import dev.ridill.mym.expense.domain.notification.AutoAddExpenseNotificationHelper
import dev.ridill.mym.expense.domain.repository.ExpenseRepository
import dev.ridill.mym.expense.domain.repository.TagsRepository
import dev.ridill.mym.expense.domain.sms.ExpenseSmsService
import dev.ridill.mym.expense.presentation.addEditExpense.AddEditExpenseViewModel
import dev.ridill.mym.expense.presentation.allExpenses.AllExpensesViewModel
import kotlinx.coroutines.CoroutineScope

@Module
@InstallIn(SingletonComponent::class)
object TransactionModule {

    @Provides
    fun provideTransactionDao(db: RivoDatabase): TransactionDao = db.transactionDao()

    @Provides
    fun provideTagsDao(db: RivoDatabase): TagsDao = db.tagsDao()

    @Provides
    fun provideExpenseRepository(
        dao: TransactionDao,
        preferencesManager: PreferencesManager
    ): ExpenseRepository = ExpenseRepositoryImpl(
        dao = dao,
        preferencesManager = preferencesManager
    )

    @Provides
    fun provideTagsRepository(dao: TagsDao): TagsRepository = TagsRepositoryImpl(dao)

    @Provides
    fun provideAddEditExpenseEventBus(): EventBus<AddEditExpenseViewModel.AddEditExpenseEvent> =
        EventBus()

    @Provides
    fun provideAllExpenseEventBus(): EventBus<AllExpensesViewModel.AllExpenseEvent> = EventBus()

    @Provides
    fun provideExpenseSmsService(
        expenseRepository: ExpenseRepository,
        notificationHelper: AutoAddExpenseNotificationHelper,
        @ApplicationScope applicationScope: CoroutineScope,
        @ApplicationContext context: Context
    ): ExpenseSmsService = ExpenseSmsService(
        repo = expenseRepository,
        notificationHelper = notificationHelper,
        applicationScope = applicationScope,
        context = context
    )

    @Provides
    fun provideExpenseNotificationHelper(
        @ApplicationContext context: Context
    ): AutoAddExpenseNotificationHelper = AutoAddExpenseNotificationHelper(context)
}