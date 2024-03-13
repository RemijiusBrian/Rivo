package dev.ridill.rivo.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dev.ridill.rivo.core.data.db.RivoDatabase
import dev.ridill.rivo.core.domain.notification.NotificationHelper
import dev.ridill.rivo.core.domain.util.EventBus
import dev.ridill.rivo.transactionSchedules.data.local.TxSchedulesDao
import dev.ridill.rivo.transactionSchedules.data.repository.SchedulesRepositoryImpl
import dev.ridill.rivo.transactionSchedules.data.repository.SchedulesAndPlansRepositoryImpl
import dev.ridill.rivo.transactionSchedules.domain.model.TxSchedule
import dev.ridill.rivo.transactionSchedules.domain.notification.TxScheduleNotificationHelper
import dev.ridill.rivo.transactionSchedules.domain.repository.SchedulesRepository
import dev.ridill.rivo.transactionSchedules.domain.repository.SchedulesAndPlansRepository
import dev.ridill.rivo.transactionSchedules.domain.transactionScheduler.AlarmManagerTransactionScheduler
import dev.ridill.rivo.transactionSchedules.domain.transactionScheduler.TransactionScheduler
import dev.ridill.rivo.transactionSchedules.presentation.schedulesAndPlansList.SchedulesAndPlansListEvent
import dev.ridill.rivo.transactions.domain.repository.AddEditTransactionRepository

@Module
@InstallIn(SingletonComponent::class)
object ScheduledTransactionsModule {

    @Provides
    fun provideTxScheduleDao(database: RivoDatabase): TxSchedulesDao =
        database.txScheduleDao()

    @Provides
    fun provideTransactionScheduler(
        @ApplicationContext context: Context
    ): TransactionScheduler = AlarmManagerTransactionScheduler(context)

    @Provides
    fun provideScheduledTransactionRepository(
        dao: TxSchedulesDao,
        scheduler: TransactionScheduler
    ): SchedulesRepository = SchedulesRepositoryImpl(
        dao = dao,
        scheduler = scheduler
    )

    @Provides
    fun provideScheduledTransactionNotificationHelper(
        @ApplicationContext context: Context
    ): NotificationHelper<TxSchedule> = TxScheduleNotificationHelper(context)

    @Provides
    fun provideSchedulesAndPlansRepository(
        dao: TxSchedulesDao,
        addEditTransactionRepository: AddEditTransactionRepository
    ): SchedulesAndPlansRepository = SchedulesAndPlansRepositoryImpl(
        dao = dao,
        transactionRepository = addEditTransactionRepository
    )

    @Provides
    fun provideSchedulesAndPlansEventBus(): EventBus<SchedulesAndPlansListEvent> = EventBus()
}