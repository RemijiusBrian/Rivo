package dev.ridill.rivo.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dev.ridill.rivo.application.RivoViewModel
import dev.ridill.rivo.core.data.db.MIGRATION_11_12
import dev.ridill.rivo.core.data.db.RivoDatabase
import dev.ridill.rivo.core.data.preferences.PreferencesManager
import dev.ridill.rivo.core.data.preferences.PreferencesManagerImpl
import dev.ridill.rivo.core.domain.crypto.CryptoManager
import dev.ridill.rivo.core.domain.crypto.DefaultCryptoManager
import dev.ridill.rivo.core.domain.service.ExpEvalService
import dev.ridill.rivo.core.domain.service.ReceiverService
import dev.ridill.rivo.core.domain.util.EventBus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import javax.inject.Qualifier
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Singleton
    @Provides
    fun provideRivoDatabase(
        @ApplicationContext context: Context
    ): RivoDatabase = Room
        .databaseBuilder(
            context = context,
            klass = RivoDatabase::class.java,
            name = RivoDatabase.NAME
        )
        .addMigrations(MIGRATION_11_12)
        .fallbackToDestructiveMigration()
        .build()

    @Singleton
    @Provides
    fun provideDataStoreInstance(
        @ApplicationContext context: Context
    ): DataStore<Preferences> = PreferenceDataStoreFactory.create(
        corruptionHandler = ReplaceFileCorruptionHandler { emptyPreferences() },
        produceFile = { context.preferencesDataStoreFile(PreferencesManager.NAME) }
    )

    @Provides
    fun providePreferencesManager(
        dataStore: DataStore<Preferences>
    ): PreferencesManager = PreferencesManagerImpl(dataStore)

    @Provides
    fun provideExpressionEvaluationService(): ExpEvalService = ExpEvalService()

    @ApplicationScope
    @Provides
    fun provideApplicationScope(): CoroutineScope = CoroutineScope(SupervisorJob())

    @Provides
    fun provideReceiverService(
        @ApplicationContext context: Context
    ): ReceiverService = ReceiverService(context)

    @Provides
    fun provideRivoEventBus(): EventBus<RivoViewModel.RivoEvent> = EventBus()

    @Provides
    fun provideCryptoManager(): CryptoManager = DefaultCryptoManager()
}

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ApplicationScope