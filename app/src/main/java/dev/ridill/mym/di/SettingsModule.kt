package dev.ridill.mym.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.ridill.mym.core.domain.service.AppDistributionService
import dev.ridill.mym.core.domain.util.EventBus
import dev.ridill.mym.settings.presentation.settings.SettingsViewModel

@Module
@InstallIn(ViewModelComponent::class)
object SettingsModule {

    @Provides
    fun provideSettingsEventBus(): EventBus<SettingsViewModel.SettingsEvent> = EventBus()

    @Provides
    fun provideAppDistributionService(
        @ApplicationContext context: Context
    ): AppDistributionService = AppDistributionService(context)
}