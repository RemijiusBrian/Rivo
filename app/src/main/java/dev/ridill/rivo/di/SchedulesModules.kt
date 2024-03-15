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
import dev.ridill.rivo.schedules.data.local.SchedulesDao
import dev.ridill.rivo.schedules.data.repository.SchedulesAndPlansRepositoryImpl
import dev.ridill.rivo.schedules.data.repository.SchedulesRepositoryImpl
import dev.ridill.rivo.schedules.domain.model.Schedule
import dev.ridill.rivo.schedules.domain.notification.ScheduleReminderNotificationHelper
import dev.ridill.rivo.schedules.domain.repository.SchedulesAndPlansRepository
import dev.ridill.rivo.schedules.domain.repository.SchedulesRepository
import dev.ridill.rivo.schedules.domain.scheduleReminder.AlarmManagerScheduleReminder
import dev.ridill.rivo.schedules.domain.scheduleReminder.ScheduleReminder
import dev.ridill.rivo.schedules.presentation.schedulesAndPlansList.SchedulesAndPlansListEvent
import dev.ridill.rivo.transactions.domain.repository.AddEditTransactionRepository

@Module
@InstallIn(SingletonComponent::class)
object SchedulesModules {

    @Provides
    fun provideSchedulesDao(database: RivoDatabase): SchedulesDao =
        database.schedulesDao()

    @Provides
    fun provideScheduleReminder(
        @ApplicationContext context: Context
    ): ScheduleReminder = AlarmManagerScheduleReminder(context)

    @Provides
    fun provideSchedulesRepository(
        dao: SchedulesDao,
        scheduler: ScheduleReminder,
        receiverService: ReceiverService
    ): SchedulesRepository = SchedulesRepositoryImpl(
        dao = dao,
        scheduler = scheduler,
        receiverService = receiverService
    )

    @Provides
    fun provideScheduleReminderNotificationHelper(
        @ApplicationContext context: Context
    ): NotificationHelper<Schedule> = ScheduleReminderNotificationHelper(context)

    @Provides
    fun provideSchedulesAndPlansRepository(
        db: RivoDatabase,
        dao: SchedulesDao,
        addEditTransactionRepository: AddEditTransactionRepository
    ): SchedulesAndPlansRepository = SchedulesAndPlansRepositoryImpl(
        db = db,
        dao = dao,
        transactionRepository = addEditTransactionRepository
    )

    @Provides
    fun provideSchedulesAndPlansEventBus(): EventBus<SchedulesAndPlansListEvent> = EventBus()
}