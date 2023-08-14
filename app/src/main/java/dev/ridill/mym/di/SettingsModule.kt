package dev.ridill.mym.di

import android.content.Context
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dev.ridill.mym.BuildConfig
import dev.ridill.mym.core.data.db.MYMDatabase
import dev.ridill.mym.core.data.preferences.PreferencesManager
import dev.ridill.mym.core.domain.service.GoogleSignInService
import dev.ridill.mym.core.domain.util.EventBus
import dev.ridill.mym.settings.data.remote.GDriveApi
import dev.ridill.mym.settings.data.repository.BackupRepositoryImpl
import dev.ridill.mym.settings.domain.backup.BackupService
import dev.ridill.mym.settings.domain.backup.BackupWorkManager
import dev.ridill.mym.settings.domain.notification.BackupNotificationHelper
import dev.ridill.mym.settings.domain.repositoty.BackupRepository
import dev.ridill.mym.settings.presentation.backupSettings.BackupSettingsViewModel
import dev.ridill.mym.settings.presentation.settings.SettingsViewModel
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Qualifier

@Module
@InstallIn(SingletonComponent::class)
object SettingsModule {

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
        database: MYMDatabase
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