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
import dev.ridill.rivo.core.domain.service.GoogleSignInService
import dev.ridill.rivo.core.domain.util.EventBus
import dev.ridill.rivo.settings.data.local.ConfigDao
import dev.ridill.rivo.settings.data.remote.GDriveApi
import dev.ridill.rivo.settings.data.remote.interceptors.GoogleAccessTokenInterceptor
import dev.ridill.rivo.settings.data.repository.BackupRepositoryImpl
import dev.ridill.rivo.settings.data.repository.BackupSettingsRepositoryImpl
import dev.ridill.rivo.settings.data.repository.SettingsRepositoryImpl
import dev.ridill.rivo.settings.domain.appLock.AppLockServiceManager
import dev.ridill.rivo.settings.domain.backup.BackupService
import dev.ridill.rivo.settings.domain.backup.BackupWorkManager
import dev.ridill.rivo.settings.domain.notification.AppLockNotificationHelper
import dev.ridill.rivo.settings.domain.notification.BackupNotificationHelper
import dev.ridill.rivo.settings.domain.repositoty.BackupRepository
import dev.ridill.rivo.settings.domain.repositoty.BackupSettingsRepository
import dev.ridill.rivo.settings.domain.repositoty.SettingsRepository
import dev.ridill.rivo.settings.presentation.backup.BackupSettingsViewModel
import dev.ridill.rivo.settings.presentation.backupEncryption.BackupEncryptionViewModel
import dev.ridill.rivo.settings.presentation.security.SecuritySettingsViewModel
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

    @Provides
    fun provideBackupSettingsRepository(
        dao: ConfigDao,
        signInService: GoogleSignInService,
        preferencesManager: PreferencesManager,
        backupWorkManager: BackupWorkManager,
        cryptoManager: CryptoManager
    ): BackupSettingsRepository = BackupSettingsRepositoryImpl(
        dao = dao,
        signInService = signInService,
        preferencesManager = preferencesManager,
        backupWorkManager = backupWorkManager,
        cryptoManager = cryptoManager
    )

    @Provides
    fun provideAppLockNotificationHelper(
        @ApplicationContext context: Context
    ): AppLockNotificationHelper = AppLockNotificationHelper(context)

    @Provides
    fun provideAppLockServiceManager(
        @ApplicationContext context: Context
    ): AppLockServiceManager = AppLockServiceManager(context)

    @Provides
    fun provideSecuritySettingsEventBus(): EventBus<SecuritySettingsViewModel.SecuritySettingsEvent> =
        EventBus()

    @Provides
    fun provideBackupEncryptionEventBus(): EventBus<BackupEncryptionViewModel.BackupEncryptionEvent> =
        EventBus()
}

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class GoogleApis