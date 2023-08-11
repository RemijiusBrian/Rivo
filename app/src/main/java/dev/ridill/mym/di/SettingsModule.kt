package dev.ridill.mym.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.ridill.mym.core.data.db.MYMDatabase
import dev.ridill.mym.core.domain.service.AppDistributionService
import dev.ridill.mym.core.domain.util.EventBus
import dev.ridill.mym.settings.domain.service.BackupService
import dev.ridill.mym.settings.domain.service.GoogleSignInService
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

    @Provides
    fun provideBackupService(
        @ApplicationContext context: Context,
        database: MYMDatabase
    ): BackupService = BackupService(context, database)

    @Provides
    fun provideGoogleSignInService(
        @ApplicationContext context: Context
    ): GoogleSignInService = GoogleSignInService(context)
}