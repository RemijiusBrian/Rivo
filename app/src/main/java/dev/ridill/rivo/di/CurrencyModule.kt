package dev.ridill.rivo.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.ridill.rivo.core.data.db.RivoDatabase
import dev.ridill.rivo.core.domain.util.EventBus
import dev.ridill.rivo.settings.data.local.CurrencyDao
import dev.ridill.rivo.settings.data.local.CurrencyPreferenceDao
import dev.ridill.rivo.settings.data.repository.CurrencyPreferenceRepositoryImpl
import dev.ridill.rivo.settings.domain.repositoty.CurrencyPreferenceRepository
import dev.ridill.rivo.settings.presentation.currencyUpdate.UpdateCurrencyViewModel

@Module
@InstallIn(SingletonComponent::class)
object CurrencyModule {

    @Provides
    fun provideCurrencyDao(database: RivoDatabase): CurrencyDao =
        database.currencyDao()

    @Provides
    fun provideCurrencyPreferenceDao(database: RivoDatabase): CurrencyPreferenceDao =
        database.currencyPreferenceDao()

    @Provides
    fun provideCurrencyPreferenceRepository(
        dao: CurrencyPreferenceDao,
        currencyDao: CurrencyDao
    ): CurrencyPreferenceRepository = CurrencyPreferenceRepositoryImpl(
        dao = dao,
        currencyDao = currencyDao
    )

    @Provides
    fun provideUpdateCurrencyEventBus(): EventBus<UpdateCurrencyViewModel.UpdateCurrencyEvent> =
        EventBus()
}