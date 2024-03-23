package dev.ridill.rivo.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dev.ridill.rivo.core.data.db.RivoDatabase
import dev.ridill.rivo.core.domain.notification.NotificationHelper
import dev.ridill.rivo.settings.domain.repositoty.CurrencyRepository
import dev.ridill.rivo.transactions.data.local.TransactionDao
import dev.ridill.rivo.transactions.data.repository.TransactionRepositoryImpl
import dev.ridill.rivo.transactions.domain.model.Transaction
import dev.ridill.rivo.transactions.domain.notification.AutoAddTransactionNotificationHelper
import dev.ridill.rivo.transactions.domain.notification.AutoAddTxSetupNotificationHelper
import dev.ridill.rivo.transactions.domain.repository.TransactionRepository
import dev.ridill.rivo.transactions.domain.sms.SMSModelDownloadManager
import dev.ridill.rivo.transactions.domain.sms.TransactionSmsService
import kotlinx.coroutines.CoroutineScope
import javax.inject.Qualifier

@Module
@InstallIn(SingletonComponent::class)
object TransactionSingletonModule {
    @Provides
    fun provideTransactionDao(db: RivoDatabase): TransactionDao = db.transactionDao()

    @Provides
    fun provideTransactionRepository(
        dao: TransactionDao
    ): TransactionRepository = TransactionRepositoryImpl(dao)

    @Provides
    fun provideTransactionSmsService(
        currencyRepository: CurrencyRepository,
        transactionRepository: TransactionRepository,
        notificationHelper: NotificationHelper<Transaction>,
        @ApplicationScope applicationScope: CoroutineScope,
        @ApplicationContext context: Context
    ): TransactionSmsService = TransactionSmsService(
        currencyRepo = currencyRepository,
        transactionRepo = transactionRepository,
        notificationHelper = notificationHelper,
        applicationScope = applicationScope,
        context = context
    )

    @Provides
    fun provideAutoAddTransactionNotificationHelper(
        @ApplicationContext context: Context
    ): NotificationHelper<Transaction> = AutoAddTransactionNotificationHelper(context)

    @AutoAddTransaction
    @Provides
    fun provideAutoAddTxSetupNotificationHelper(
        @ApplicationContext context: Context
    ): NotificationHelper<Unit> = AutoAddTxSetupNotificationHelper(context)

    @Provides
    fun provideTransactionSMSModelDownloadManager(
        @ApplicationContext context: Context
    ): SMSModelDownloadManager = SMSModelDownloadManager(context)
}

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class AutoAddTransaction