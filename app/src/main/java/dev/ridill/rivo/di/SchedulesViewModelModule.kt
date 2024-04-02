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
import dev.ridill.rivo.schedules.domain.repository.SchedulesRepository
import dev.ridill.rivo.schedules.presentation.allSchedules.AllSchedulesViewModel

@Module
@InstallIn(ViewModelComponent::class)
object SchedulesViewModelModule {
    @Provides
    fun provideAllSchedulesRepository(
        db: RivoDatabase,
        schedulesDao: SchedulesDao,
        schedulesRepository: SchedulesRepository
    ): AllSchedulesRepository = AllSchedulesRepositoryImpl(
        db = db,
        schedulesDao = schedulesDao,
        repo = schedulesRepository
    )

    @Provides
    fun provideAllSchedulesEventBuss(): EventBus<AllSchedulesViewModel.AllSchedulesEvent> =
        EventBus()
}