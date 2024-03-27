package dev.ridill.rivo.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dev.ridill.rivo.core.data.db.RivoDatabase
import dev.ridill.rivo.core.data.preferences.PreferencesManager
import dev.ridill.rivo.core.domain.crypto.CryptoManager
import dev.ridill.rivo.core.domain.service.GoogleSignInService
import dev.ridill.rivo.core.domain.util.EventBus
import dev.ridill.rivo.settings.data.local.BudgetDao
import dev.ridill.rivo.settings.data.local.ConfigDao
import dev.ridill.rivo.settings.data.repository.BackupSettingsRepositoryImpl
import dev.ridill.rivo.settings.data.repository.BudgetRepositoryImpl
import dev.ridill.rivo.settings.data.repository.SettingsRepositoryImpl
import dev.ridill.rivo.settings.domain.backup.BackupWorkManager
import dev.ridill.rivo.settings.domain.repositoty.BackupSettingsRepository
import dev.ridill.rivo.settings.domain.repositoty.BudgetRepository
import dev.ridill.rivo.settings.domain.repositoty.CurrencyRepository
import dev.ridill.rivo.settings.domain.repositoty.SettingsRepository
import dev.ridill.rivo.settings.presentation.backupSettings.BackupSettingsViewModel
import dev.ridill.rivo.settings.presentation.backupEncryption.BackupEncryptionViewModel
import dev.ridill.rivo.settings.presentation.securitySettings.SecuritySettingsViewModel
import dev.ridill.rivo.settings.presentation.settings.SettingsViewModel

@Module
@InstallIn(ViewModelComponent::class)
object SettingsViewModelModule {
    @Provides
    fun provideBudgetDao(database: RivoDatabase): BudgetDao = database.budgetDao()

    @Provides
    fun provideBudgetRepository(
        dao: BudgetDao
    ): BudgetRepository = BudgetRepositoryImpl(dao)

    @Provides
    fun provideMiscConfigDao(database: RivoDatabase): ConfigDao = database.configDao()

    @Provides
    fun provideSettingsRepository(
        budgetRepository: BudgetRepository,
        currencyRepository: CurrencyRepository
    ): SettingsRepository = SettingsRepositoryImpl(
        budgetRepo = budgetRepository,
        currencyRepo = currencyRepository
    )

    @Provides
    fun provideSettingsEventBus(): EventBus<SettingsViewModel.SettingsEvent> = EventBus()

    @Provides
    fun provideBackupSettingsEventBus(): EventBus<BackupSettingsViewModel.BackupSettingsEvent> = EventBus()

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
    fun provideSecuritySettingsEventBus(): EventBus<SecuritySettingsViewModel.SecuritySettingsEvent> =
        EventBus()

    @Provides
    fun provideBackupEncryptionEventBus(): EventBus<BackupEncryptionViewModel.BackupEncryptionEvent> =
        EventBus()
}