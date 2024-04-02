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
import dev.ridill.rivo.core.domain.crypto.CryptoManager
import dev.ridill.rivo.core.domain.notification.NotificationHelper
import dev.ridill.rivo.core.domain.service.GoogleSignInService
import dev.ridill.rivo.schedules.domain.repository.SchedulesRepository
import dev.ridill.rivo.settings.data.local.ConfigDao
import dev.ridill.rivo.settings.data.local.CurrencyDao
import dev.ridill.rivo.settings.data.remote.GDriveApi
import dev.ridill.rivo.settings.data.remote.interceptors.GoogleAccessTokenInterceptor
import dev.ridill.rivo.settings.data.repository.BackupRepositoryImpl
import dev.ridill.rivo.settings.data.repository.CurrencyRepositoryImpl
import dev.ridill.rivo.settings.domain.appLock.AppLockServiceManager
import dev.ridill.rivo.settings.domain.backup.BackupService
import dev.ridill.rivo.settings.domain.backup.BackupWorkManager
import dev.ridill.rivo.settings.domain.notification.BackupNotificationHelper
import dev.ridill.rivo.settings.domain.repositoty.BackupRepository
import dev.ridill.rivo.settings.domain.repositoty.CurrencyRepository
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Qualifier
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SettingsSingletonModule {
    @Provides
    fun provideCurrencyDao(database: RivoDatabase): CurrencyDao = database.currencyDao()

    @Provides
    fun provideCurrencyRepository(
        dao: CurrencyDao
    ): CurrencyRepository = CurrencyRepositoryImpl(dao)

    @Provides
    fun provideGoogleSignInService(
        @ApplicationContext context: Context
    ): GoogleSignInService = GoogleSignInService(context)

    @GoogleApis
    @Provides
    fun provideGoogleAccessTokenInterceptor(
        signInService: GoogleSignInService
    ): GoogleAccessTokenInterceptor = GoogleAccessTokenInterceptor(signInService)

    @Provides
    fun provideLoggingInterceptor(): HttpLoggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    @GoogleApis
    @Provides
    fun provideGoogleApisHttpClient(
        @GoogleApis googleAccessTokenInterceptor: GoogleAccessTokenInterceptor,
        loggingInterceptor: HttpLoggingInterceptor
    ): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(googleAccessTokenInterceptor)
        .addInterceptor(loggingInterceptor)
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
        database: RivoDatabase,
        cryptoManager: CryptoManager
    ): BackupService = BackupService(
        context = context,
        database = database,
        cryptoManager = cryptoManager
    )

    @Provides
    fun provideBackupRepository(
        backupService: BackupService,
        gDriveApi: GDriveApi,
        signInService: GoogleSignInService,
        preferencesManager: PreferencesManager,
        configDao: ConfigDao,
        backupWorkManager: BackupWorkManager,
        schedulesRepository: SchedulesRepository
    ): BackupRepository = BackupRepositoryImpl(
        backupService = backupService,
        gDriveApi = gDriveApi,
        signInService = signInService,
        preferencesManager = preferencesManager,
        configDao = configDao,
        backupWorkManager = backupWorkManager,
        schedulesRepository = schedulesRepository
    )

    @Provides
    fun provideBackupWorkManager(
        @ApplicationContext context: Context
    ): BackupWorkManager = BackupWorkManager(context)

    @BackupFeature
    @Provides
    fun provideBackupNotificationHelper(
        @ApplicationContext context: Context
    ): NotificationHelper<String> = BackupNotificationHelper(context)

    @Provides
    fun provideAppLockServiceManager(
        @ApplicationContext context: Context
    ): AppLockServiceManager = AppLockServiceManager(context)
}

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class GoogleApis

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class BackupFeature