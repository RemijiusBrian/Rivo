package dev.ridill.rivo.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dev.ridill.rivo.core.data.db.RivoDatabase
import dev.ridill.rivo.core.domain.util.EventBus
import dev.ridill.rivo.settings.data.local.ConfigDao
import dev.ridill.rivo.transactionFolders.data.local.TransactionFolderDao
import dev.ridill.rivo.transactionFolders.data.repository.FolderDetailsRepositoryImpl
import dev.ridill.rivo.transactionFolders.data.repository.FoldersListRepositoryImpl
import dev.ridill.rivo.transactionFolders.domain.repository.FolderDetailsRepository
import dev.ridill.rivo.transactionFolders.domain.repository.FoldersListRepository
import dev.ridill.rivo.transactionFolders.presentation.transactionFolderDetails.TxFolderDetailsViewModel
import dev.ridill.rivo.transactions.data.local.TransactionDao

@Module
@InstallIn(ViewModelComponent::class)
object TransactionFolderModule {

    @Provides
    fun provideTransactionFolderDao(db: RivoDatabase): TransactionFolderDao =
        db.transactionFolderDao()

    @Provides
    fun provideTxFoldersListRepository(
        txFolderDao: TransactionFolderDao,
        configDao: ConfigDao
    ): FoldersListRepository = FoldersListRepositoryImpl(
        folderDao = txFolderDao,
        configDao = configDao
    )

    @Provides
    fun provideTxFolderDetailsRepository(
        transactionFolderDao: TransactionFolderDao,
        transactionDao: TransactionDao
    ): FolderDetailsRepository = FolderDetailsRepositoryImpl(
        dao = transactionFolderDao,
        transactionDao = transactionDao
    )

    @Provides
    fun provideTxFolderDetailsEventBus(): EventBus<TxFolderDetailsViewModel.TxFolderDetailsEvent> =
        EventBus()
}