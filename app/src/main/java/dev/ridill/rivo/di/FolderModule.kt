package dev.ridill.rivo.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dev.ridill.rivo.core.data.db.RivoDatabase
import dev.ridill.rivo.core.domain.util.EventBus
import dev.ridill.rivo.folders.data.local.FolderDao
import dev.ridill.rivo.folders.data.repository.FolderDetailsRepositoryImpl
import dev.ridill.rivo.folders.data.repository.FoldersListRepositoryImpl
import dev.ridill.rivo.folders.domain.repository.FolderDetailsRepository
import dev.ridill.rivo.folders.domain.repository.FoldersListRepository
import dev.ridill.rivo.folders.presentation.folderDetails.FolderDetailsViewModel
import dev.ridill.rivo.settings.data.local.ConfigDao
import dev.ridill.rivo.transactions.data.local.TransactionDao

@Module
@InstallIn(ViewModelComponent::class)
object FolderModule {

    @Provides
    fun provideFolderDao(db: RivoDatabase): FolderDao = db.folderDao()

    @Provides
    fun provideFoldersListRepository(
        txFolderDao: FolderDao,
        configDao: ConfigDao
    ): FoldersListRepository = FoldersListRepositoryImpl(
        folderDao = txFolderDao,
        configDao = configDao
    )

    @Provides
    fun provideFolderDetailsRepository(
        folderDao: FolderDao,
        transactionDao: TransactionDao
    ): FolderDetailsRepository = FolderDetailsRepositoryImpl(
        dao = folderDao,
        transactionDao = transactionDao
    )

    @Provides
    fun provideFolderDetailsEventBus(): EventBus<FolderDetailsViewModel.FolderDetailsEvent> =
        EventBus()
}