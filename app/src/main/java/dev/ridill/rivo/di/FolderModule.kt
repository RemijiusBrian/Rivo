package dev.ridill.rivo.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dev.ridill.rivo.core.data.db.RivoDatabase
import dev.ridill.rivo.core.domain.util.EventBus
import dev.ridill.rivo.folders.data.local.FolderDao
import dev.ridill.rivo.folders.data.repository.AddEditFolderRepositoryImpl
import dev.ridill.rivo.folders.data.repository.FolderDetailsRepositoryImpl
import dev.ridill.rivo.folders.data.repository.AllFoldersRepositoryImpl
import dev.ridill.rivo.folders.domain.repository.AddEditFolderRepository
import dev.ridill.rivo.folders.domain.repository.FolderDetailsRepository
import dev.ridill.rivo.folders.domain.repository.AllFoldersRepository
import dev.ridill.rivo.folders.presentation.addEditFolder.AddEditFolderViewModel
import dev.ridill.rivo.folders.presentation.folderDetails.FolderDetailsViewModel
import dev.ridill.rivo.transactions.data.local.TransactionDao

@Module
@InstallIn(ViewModelComponent::class)
object FolderModule {

    @Provides
    fun provideFolderDao(db: RivoDatabase): FolderDao = db.folderDao()

    @Provides
    fun provideAllFoldersRepository(
        folderDao: FolderDao
    ): AllFoldersRepository = AllFoldersRepositoryImpl(
        folderDao = folderDao
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

    @Provides
    fun provideAddEditFolderRepository(
        dao: FolderDao
    ): AddEditFolderRepository = AddEditFolderRepositoryImpl(
        dao = dao
    )

    @Provides
    fun provideAddEditFolderEventBus(): EventBus<AddEditFolderViewModel.AddEditFolderEvent> =
        EventBus()
}