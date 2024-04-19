package dev.ridill.rivo.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dev.ridill.rivo.core.data.db.RivoDatabase
import dev.ridill.rivo.core.data.preferences.PreferencesManager
import dev.ridill.rivo.core.domain.util.EventBus
import dev.ridill.rivo.schedules.domain.repository.SchedulesRepository
import dev.ridill.rivo.settings.domain.repositoty.CurrencyPreferenceRepository
import dev.ridill.rivo.transactions.data.local.TagsDao
import dev.ridill.rivo.transactions.data.local.TransactionDao
import dev.ridill.rivo.transactions.data.repository.AddEditTransactionRepositoryImpl
import dev.ridill.rivo.transactions.data.repository.AllTransactionsRepositoryImpl
import dev.ridill.rivo.transactions.data.repository.TagsRepositoryImpl
import dev.ridill.rivo.transactions.domain.repository.AddEditTransactionRepository
import dev.ridill.rivo.transactions.domain.repository.AllTransactionsRepository
import dev.ridill.rivo.transactions.domain.repository.TagsRepository
import dev.ridill.rivo.transactions.presentation.addEditTransaction.AddEditTransactionViewModel
import dev.ridill.rivo.transactions.presentation.allTransactions.AllTransactionsViewModel

@Module
@InstallIn(ViewModelComponent::class)
object TransactionViewModelModule {
    @Provides
    fun provideTagsDao(db: RivoDatabase): TagsDao = db.tagsDao()

    @Provides
    fun provideAddEditTransactionRepository(
        db: RivoDatabase,
        dao: TransactionDao,
        schedulesRepository: SchedulesRepository
    ): AddEditTransactionRepository = AddEditTransactionRepositoryImpl(
        db = db,
        dao = dao,
        schedulesRepo = schedulesRepository
    )

    @Provides
    fun provideTagsRepository(dao: TagsDao): TagsRepository = TagsRepositoryImpl(dao)

    @Provides
    fun provideAddEditTransactionEventBus(): EventBus<AddEditTransactionViewModel.AddEditTransactionEvent> =
        EventBus()

    @Provides
    fun provideAllTransactionsRepository(
        dao: TransactionDao,
        preferencesManager: PreferencesManager,
        currencyPreferenceRepository: CurrencyPreferenceRepository
    ): AllTransactionsRepository = AllTransactionsRepositoryImpl(
        dao = dao,
        preferencesManager = preferencesManager,
        currencyPrefRepo = currencyPreferenceRepository
    )

    @Provides
    fun provideAllTransactionEventBus(): EventBus<AllTransactionsViewModel.AllTransactionsEvent> =
        EventBus()
}