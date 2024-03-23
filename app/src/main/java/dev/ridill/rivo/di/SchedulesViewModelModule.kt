package dev.ridill.rivo.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dev.ridill.rivo.core.data.db.RivoDatabase
import dev.ridill.rivo.core.domain.util.EventBus
import dev.ridill.rivo.schedules.data.local.SchedulesDao
import dev.ridill.rivo.schedules.data.repository.AllSchedulesRepositoryImpl
import dev.ridill.rivo.schedules.domain.repository.AllSchedulesRepository
import dev.ridill.rivo.schedules.presentation.allSchedules.AllSchedulesViewModel
import dev.ridill.rivo.transactions.domain.repository.AddEditTransactionRepository

@Module
@InstallIn(ViewModelComponent::class)
object SchedulesViewModelModule {
    @Provides
    fun provideAllSchedulesRepository(
        db: RivoDatabase,
        schedulesDao: SchedulesDao,
        addEditTransactionRepository: AddEditTransactionRepository
    ): AllSchedulesRepository = AllSchedulesRepositoryImpl(
        db = db,
        schedulesDao = schedulesDao,
        transactionRepository = addEditTransactionRepository
    )

    @Provides
    fun provideAllSchedulesEventBuss(): EventBus<AllSchedulesViewModel.AllSchedulesEvent> =
        EventBus()
}