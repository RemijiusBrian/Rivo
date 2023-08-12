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
import dev.ridill.mym.core.domain.service.AppDistributionService
import dev.ridill.mym.core.domain.service.GoogleSignInService
import dev.ridill.mym.core.domain.util.EventBus
import dev.ridill.mym.settings.data.remote.GDriveApi
import dev.ridill.mym.settings.data.repository.BackupRepositoryImpl
import dev.ridill.mym.settings.domain.BackupRepository
import dev.ridill.mym.settings.domain.backup.BackupWorkManager
import dev.ridill.mym.settings.domain.backup.LocalDataService
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
    fun provideAppDistributionService(
        @ApplicationContext context: Context
    ): AppDistributionService = AppDistributionService(context)

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
    fun provideLocalDataService(
        @ApplicationContext context: Context,
        database: MYMDatabase
    ): LocalDataService = LocalDataService(context, database)

    @Provides
    fun provideBackupRepository(
        localDataService: LocalDataService,
        gDriveApi: GDriveApi,
        signInService: GoogleSignInService
    ): BackupRepository = BackupRepositoryImpl(
        localDataService = localDataService,
        gDriveApi = gDriveApi,
        signInService = signInService
    )

    @Provides
    fun provideBackupWorkManager(
        @ApplicationContext context: Context
    ): BackupWorkManager = BackupWorkManager(context)
}

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class GoogleApis