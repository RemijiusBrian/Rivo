package dev.ridill.rivo.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dev.ridill.rivo.core.data.db.RivoDatabase
import dev.ridill.rivo.core.data.preferences.PreferencesManager
import dev.ridill.rivo.core.domain.util.EventBus
import dev.ridill.rivo.settings.domain.repositoty.SettingsRepository
import dev.ridill.rivo.transactions.data.local.TagsDao
import dev.ridill.rivo.transactions.data.local.TransactionDao
import dev.ridill.rivo.transactions.data.repository.AddEditTransactionRepositoryImpl
import dev.ridill.rivo.transactions.data.repository.AllTransactionsRepositoryImpl
import dev.ridill.rivo.transactions.data.repository.TagsRepositoryImpl
import dev.ridill.rivo.transactions.domain.notification.AutoAddTransactionNotificationHelper
import dev.ridill.rivo.transactions.domain.notification.AutoAddTxSetupNotificationHelper
import dev.ridill.rivo.transactions.domain.repository.AddEditTransactionRepository
import dev.ridill.rivo.transactions.domain.repository.AllTransactionsRepository
import dev.ridill.rivo.transactions.domain.repository.TagsRepository
import dev.ridill.rivo.transactions.domain.sms.SMSModelDownloadManager
import dev.ridill.rivo.transactions.domain.sms.TransactionSmsService
import dev.ridill.rivo.transactions.presentation.addEditTransaction.AddEditTransactionViewModel
import dev.ridill.rivo.transactions.presentation.allTransactions.AllTransactionsViewModel
import kotlinx.coroutines.CoroutineScope

@Module
@InstallIn(SingletonComponent::class)
object TransactionModule {

    @Provides
    fun provideTransactionDao(db: RivoDatabase): TransactionDao = db.transactionDao()

    @Provides
    fun provideTagsDao(db: RivoDatabase): TagsDao = db.tagsDao()

    @Provides
    fun provideAddEditTransactionRepository(
        dao: TransactionDao
    ): AddEditTransactionRepository = AddEditTransactionRepositoryImpl(dao)

    @Provides
    fun provideTagsRepository(dao: TagsDao): TagsRepository = TagsRepositoryImpl(dao)

    @Provides
    fun provideAddEditTransactionEventBus(): EventBus<AddEditTransactionViewModel.AddEditTransactionEvent> =
        EventBus()

    @Provides
    fun provideAllTransactionsRepository(
        dao: TransactionDao,
        preferencesManager: PreferencesManager,
        settingsRepo: SettingsRepository
    ): AllTransactionsRepository = AllTransactionsRepositoryImpl(
        dao = dao,
        preferencesManager = preferencesManager,
        settingsRepo = settingsRepo
    )

    @Provides
    fun provideAllTransactionEventBus(): EventBus<AllTransactionsViewModel.AllTransactionsEvent> =
        EventBus()

    @Provides
    fun provideTransactionSmsService(
        addEditTransactionRepository: AddEditTransactionRepository,
        notificationHelper: AutoAddTransactionNotificationHelper,
        @ApplicationScope applicationScope: CoroutineScope,
        @ApplicationContext context: Context
    ): TransactionSmsService = TransactionSmsService(
        repo = addEditTransactionRepository,
        notificationHelper = notificationHelper,
        applicationScope = applicationScope,
        context = context
    )

    @Provides
    fun provideTransactionNotificationHelper(
        @ApplicationContext context: Context
    ): AutoAddTransactionNotificationHelper = AutoAddTransactionNotificationHelper(context)

    @Provides
    fun provideAutoAddTxSetupNotificationHelper(
        @ApplicationContext context: Context
    ): AutoAddTxSetupNotificationHelper = AutoAddTxSetupNotificationHelper(context)

    @Provides
    fun provideTransactionSMSModelDownloadManager(
        @ApplicationContext context: Context
    ): SMSModelDownloadManager = SMSModelDownloadManager(context)
}