package dev.ridill.rivo.di

import android.content.Context
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dev.ridill.rivo.BuildConfig
import dev.ridill.rivo.core.data.db.RivoDatabase
import dev.ridill.rivo.core.data.preferences.PreferencesManager
import dev.ridill.rivo.core.domain.service.GoogleSignInService
import dev.ridill.rivo.core.domain.util.EventBus
import dev.ridill.rivo.settings.data.local.ConfigDao
import dev.ridill.rivo.settings.data.remote.GDriveApi
import dev.ridill.rivo.settings.data.repository.BackupRepositoryImpl
import dev.ridill.rivo.settings.data.repository.SettingsRepositoryImpl
import dev.ridill.rivo.settings.domain.backup.BackupService
import dev.ridill.rivo.settings.domain.backup.BackupWorkManager
import dev.ridill.rivo.settings.domain.notification.BackupNotificationHelper
import dev.ridill.rivo.settings.domain.repositoty.BackupRepository
import dev.ridill.rivo.settings.domain.repositoty.SettingsRepository
import dev.ridill.rivo.settings.presentation.backupSettings.BackupSettingsViewModel
import dev.ridill.rivo.settings.presentation.settings.SettingsViewModel
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Qualifier
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SettingsModule {

    @Provides
    fun provideMiscConfigDao(database: RivoDatabase): ConfigDao = database.configDao()

    @Provides
    fun provideSettingsRepository(
        configDao: ConfigDao,
    ): SettingsRepository = SettingsRepositoryImpl(
        dao = configDao
    )

    @Provides
    fun provideSettingsEventBus(): EventBus<SettingsViewModel.SettingsEvent> = EventBus()

    @Provides
    fun provideGoogleSignInService(
        @ApplicationContext context: Context
    ): GoogleSignInService = GoogleSignInService(context)

    @Provides
    fun provideBackupSettingsEventBus(): EventBus<BackupSettingsViewModel.BackupEvent> = EventBus()

    @GoogleApis
    @Provides
    fun provideGoogleApisHttpClient(): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor(
                HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BODY
                }
            )
            .build()

    @GoogleApis
    @Singleton
    @Provides
    fun provideGoogleApisRetrofit(
        @GoogleApis client: OkHttpClient
    ): Retrofit = Retrofit.Builder()
        .addConverterFactory(
            GsonConverterFactory.create(
                GsonBuilder()
                    .excludeFieldsWithoutExposeAnnotation()
                    .create()
            )
        )
        .baseUrl(BuildConfig.GOOGLE_APIS_BASE_URL)
        .client(client)
        .build()

    @Provides
    fun provideGDriveApi(@GoogleApis retrofit: Retrofit): GDriveApi =
        retrofit.create(GDriveApi::class.java)

    @Provides
    fun provideBackupService(
        @ApplicationContext context: Context,
        database: RivoDatabase
    ): BackupService = BackupService(context, database)

    @Provides
    fun provideBackupRepository(
        backupService: BackupService,
        gDriveApi: GDriveApi,
        signInService: GoogleSignInService,
        preferencesManager: PreferencesManager
    ): BackupRepository = BackupRepositoryImpl(
        backupService = backupService,
        gDriveApi = gDriveApi,
        signInService = signInService,
        preferencesManager = preferencesManager
    )

    @Provides
    fun provideBackupWorkManager(
        @ApplicationContext context: Context
    ): BackupWorkManager = BackupWorkManager(context)

    @Provides
    fun provideBackupNotificationHelper(
        @ApplicationContext context: Context
    ): BackupNotificationHelper = BackupNotificationHelper(context)
}

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class GoogleApis