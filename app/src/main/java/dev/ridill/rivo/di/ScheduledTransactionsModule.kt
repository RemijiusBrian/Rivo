package dev.ridill.rivo.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dev.ridill.rivo.core.data.db.RivoDatabase
import dev.ridill.rivo.core.domain.notification.NotificationHelper
import dev.ridill.rivo.scheduledTransaction.data.local.ScheduledTransactionDao
import dev.ridill.rivo.scheduledTransaction.data.repository.ScheduledTransactionRepositoryImpl
import dev.ridill.rivo.scheduledTransaction.domain.model.ScheduledTransaction
import dev.ridill.rivo.scheduledTransaction.domain.notification.ScheduledTransactionNotificationHelper
import dev.ridill.rivo.scheduledTransaction.domain.repository.ScheduledTransactionRepository
import dev.ridill.rivo.scheduledTransaction.domain.transactionScheduler.AlarmManagerTransactionScheduler
import dev.ridill.rivo.scheduledTransaction.domain.transactionScheduler.TransactionScheduler

@Module
@InstallIn(SingletonComponent::class)
object ScheduledTransactionsModule {

    @Provides
    fun provideScheduledTransactionDao(database: RivoDatabase): ScheduledTransactionDao =
        database.scheduledTransactionDao()

    @Provides
    fun provideTransactionScheduler(
        @ApplicationContext context: Context
    ): TransactionScheduler = AlarmManagerTransactionScheduler(context)

    @Provides
    fun provideScheduledTransactionRepository(
        dao: ScheduledTransactionDao,
        scheduler: TransactionScheduler
    ): ScheduledTransactionRepository = ScheduledTransactionRepositoryImpl(
        dao = dao,
        scheduler = scheduler
    )

    @Provides
    fun provideScheduledTransactionNotificationHelper(
        @ApplicationContext context: Context
    ): NotificationHelper<ScheduledTransaction> = ScheduledTransactionNotificationHelper(context)
}