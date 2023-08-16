package dev.ridill.mym.di

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
import dev.ridill.mym.core.data.db.MIGRATION_1_2
import dev.ridill.mym.core.data.db.MYMDatabase
import dev.ridill.mym.core.data.preferences.PreferencesManager
import dev.ridill.mym.core.data.preferences.PreferencesManagerImpl
import dev.ridill.mym.core.domain.service.ExpEvalService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import javax.inject.Qualifier
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Singleton
    @Provides
    fun provideMYMDatabase(
        @ApplicationContext context: Context
    ): MYMDatabase = Room
        .databaseBuilder(
            context = context,
            klass = MYMDatabase::class.java,
            name = MYMDatabase.NAME
        )
        .addMigrations(MIGRATION_1_2)
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
    fun provideEvaluationExpressionService(): ExpEvalService = ExpEvalService()

    @ApplicationScope
    @Provides
    fun provideApplicationScope(): CoroutineScope = CoroutineScope(SupervisorJob())
}

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ApplicationScope