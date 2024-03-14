package dev.ridill.rivo.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dev.ridill.rivo.core.data.db.RivoDatabase
import dev.ridill.rivo.core.domain.notification.NotificationHelper
import dev.ridill.rivo.core.domain.service.ReceiverService
import dev.ridill.rivo.core.domain.util.EventBus
import dev.ridill.rivo.transactionSchedules.data.local.TxSchedulesDao
import dev.ridill.rivo.transactionSchedules.data.repository.SchedulesAndPlansRepositoryImpl
import dev.ridill.rivo.transactionSchedules.data.repository.SchedulesRepositoryImpl
import dev.ridill.rivo.transactionSchedules.domain.model.Schedule
import dev.ridill.rivo.transactionSchedules.domain.notification.ScheduleReminderNotificationHelper
import dev.ridill.rivo.transactionSchedules.domain.repository.SchedulesAndPlansRepository
import dev.ridill.rivo.transactionSchedules.domain.repository.SchedulesRepository
import dev.ridill.rivo.transactionSchedules.domain.scheduleReminder.AlarmManagerScheduleReminder
import dev.ridill.rivo.transactionSchedules.domain.scheduleReminder.ScheduleReminder
import dev.ridill.rivo.transactionSchedules.presentation.schedulesAndPlansList.SchedulesAndPlansListEvent
import dev.ridill.rivo.transactions.domain.repository.AddEditTransactionRepository

@Module
@InstallIn(SingletonComponent::class)
object SchedulesModules {

    @Provides
    fun provideTxScheduleDao(database: RivoDatabase): TxSchedulesDao =
        database.txScheduleDao()

    @Provides
    fun provideTransactionScheduler(
        @ApplicationContext context: Context
    ): ScheduleReminder = AlarmManagerScheduleReminder(context)

    @Provides
    fun provideScheduledTransactionRepository(
        dao: TxSchedulesDao,
        scheduler: ScheduleReminder,
        receiverService: ReceiverService
    ): SchedulesRepository = SchedulesRepositoryImpl(
        dao = dao,
        scheduler = scheduler,
        receiverService = receiverService
    )

    @Provides
    fun provideScheduledTransactionNotificationHelper(
        @ApplicationContext context: Context
    ): NotificationHelper<Schedule> = ScheduleReminderNotificationHelper(context)

    @Provides
    fun provideSchedulesAndPlansRepository(
        db: RivoDatabase,
        dao: TxSchedulesDao,
        addEditTransactionRepository: AddEditTransactionRepository
    ): SchedulesAndPlansRepository = SchedulesAndPlansRepositoryImpl(
        db = db,
        dao = dao,
        transactionRepository = addEditTransactionRepository
    )

    @Provides
    fun provideSchedulesAndPlansEventBus(): EventBus<SchedulesAndPlansListEvent> = EventBus()
}