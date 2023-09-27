package dev.ridill.rivo.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dev.ridill.rivo.core.data.db.RivoDatabase
import dev.ridill.rivo.core.domain.util.EventBus
import dev.ridill.rivo.settings.data.local.ConfigDao
import dev.ridill.rivo.transactionGroups.data.local.TransactionGroupDao
import dev.ridill.rivo.transactionGroups.data.repository.TxGroupDetailsRepositoryImpl
import dev.ridill.rivo.transactionGroups.data.repository.TxGroupsListRepositoryImpl
import dev.ridill.rivo.transactionGroups.domain.repository.TxGroupDetailsRepository
import dev.ridill.rivo.transactionGroups.domain.repository.TxGroupsListRepository
import dev.ridill.rivo.transactionGroups.presentation.groupDetails.TxGroupDetailsViewModel
import dev.ridill.rivo.transactions.data.local.TransactionDao

@Module
@InstallIn(ViewModelComponent::class)
object TransactionGroupModule {

    @Provides
    fun provideTransactionGroupDao(db: RivoDatabase): TransactionGroupDao = db.transactionGroupDao()

    @Provides
    fun provideTxGroupsListRepository(
        txGroupDao: TransactionGroupDao,
        configDao: ConfigDao
    ): TxGroupsListRepository = TxGroupsListRepositoryImpl(
        transactionGroupDao = txGroupDao,
        configDao = configDao
    )

    @Provides
    fun provideTxGroupDetailsRepository(
        transactionGroupDao: TransactionGroupDao,
        transactionDao: TransactionDao
    ): TxGroupDetailsRepository = TxGroupDetailsRepositoryImpl(
        dao = transactionGroupDao,
        transactionDao = transactionDao
    )

    @Provides
    fun provideTxGroupDetailsEventBus(): EventBus<TxGroupDetailsViewModel.TxGroupDetailsEvent> =
        EventBus()
}