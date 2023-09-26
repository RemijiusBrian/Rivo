package dev.ridill.rivo.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dev.ridill.rivo.core.data.db.RivoDatabase
import dev.ridill.rivo.transactionGroups.data.local.TransactionGroupDao

@Module
@InstallIn(ViewModelComponent::class)
object TransactionGroupModule {

    @Provides
    fun provideTransactionGroupDao(db: RivoDatabase): TransactionGroupDao = db.transactionGroupDao()
}