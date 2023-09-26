package dev.ridill.rivo.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dev.ridill.rivo.core.data.db.RivoDatabase
import dev.ridill.rivo.core.domain.util.EventBus
import dev.ridill.rivo.transactionGroups.data.local.TransactionGroupDao
import dev.ridill.rivo.transactionGroups.data.repository.TxGroupsListRepositoryImpl
import dev.ridill.rivo.transactionGroups.domain.repository.TxGroupsListRepository
import dev.ridill.rivo.transactionGroups.presentation.groupsList.TxGroupsListViewModel

@Module
@InstallIn(ViewModelComponent::class)
object TransactionGroupModule {

    @Provides
    fun provideTransactionGroupDao(db: RivoDatabase): TransactionGroupDao = db.transactionGroupDao()

    @Provides
    fun provideTxGroupsListRepository(
        dao: TransactionGroupDao
    ): TxGroupsListRepository = TxGroupsListRepositoryImpl(dao)

    @Provides
    fun providesTxGroupsListEventBus(): EventBus<TxGroupsListViewModel.TxGroupsListEvent> =
        EventBus()
}