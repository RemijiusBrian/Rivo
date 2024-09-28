package dev.ridill.rivo.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.ridill.rivo.core.data.db.RivoDatabase
import dev.ridill.rivo.core.domain.util.EventBus
import dev.ridill.rivo.settings.data.local.CurrencyListDao
import dev.ridill.rivo.settings.data.local.CurrencyPreferenceDao
import dev.ridill.rivo.settings.data.repository.CurrencyPreferenceRepositoryImpl
import dev.ridill.rivo.settings.domain.repositoty.CurrencyPreferenceRepository
import dev.ridill.rivo.settings.presentation.currencyUpdate.UpdateCurrencyPreferenceViewModel

@Module
@InstallIn(SingletonComponent::class)
object CurrencyModule {

    @Provides
    fun provideCurrencyDao(database: RivoDatabase): CurrencyListDao =
        database.currencyListDao()

    @Provides
    fun provideCurrencyPreferenceDao(database: RivoDatabase): CurrencyPreferenceDao =
        database.currencyPreferenceDao()

    @Provides
    fun provideCurrencyPreferenceRepository(
        dao: CurrencyPreferenceDao,
        currencyListDao: CurrencyListDao
    ): CurrencyPreferenceRepository = CurrencyPreferenceRepositoryImpl(
        dao = dao,
        currencyListDao = currencyListDao
    )

    @Provides
    fun provideUpdateCurrencyEventBus(): EventBus<UpdateCurrencyPreferenceViewModel.UpdateCurrencyEvent> =
        EventBus()
}