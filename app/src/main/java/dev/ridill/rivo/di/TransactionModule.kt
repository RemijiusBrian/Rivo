package dev.ridill.rivo.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dev.ridill.rivo.core.data.db.RivoDatabase
import dev.ridill.rivo.core.data.preferences.PreferencesManager
import dev.ridill.rivo.core.domain.util.EventBus
import dev.ridill.rivo.transactions.data.local.TagsDao
import dev.ridill.rivo.transactions.data.local.TransactionDao
import dev.ridill.rivo.transactions.data.repository.ExpenseRepositoryImpl
import dev.ridill.rivo.transactions.data.repository.TagsRepositoryImpl
import dev.ridill.rivo.transactions.domain.notification.AutoAddExpenseNotificationHelper
import dev.ridill.rivo.transactions.domain.repository.ExpenseRepository
import dev.ridill.rivo.transactions.domain.repository.TagsRepository
import dev.ridill.rivo.transactions.domain.sms.ExpenseSmsService
import dev.ridill.rivo.transactions.presentation.addEditExpense.AddEditExpenseViewModel
import dev.ridill.rivo.transactions.presentation.allExpenses.AllExpensesViewModel
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