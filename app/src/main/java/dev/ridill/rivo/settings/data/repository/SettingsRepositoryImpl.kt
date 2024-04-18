package dev.ridill.rivo.settings.data.repository

import dev.ridill.rivo.settings.domain.repositoty.BudgetPreferenceRepository
import dev.ridill.rivo.settings.domain.repositoty.CurrencyPreferenceRepository
import dev.ridill.rivo.settings.domain.repositoty.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import java.util.Currency

class SettingsRepositoryImpl(
    private val budgetPrefRepo: BudgetPreferenceRepository,
    private val currencyPrefRepo: CurrencyPreferenceRepository
) : SettingsRepository {
    override fun getCurrentBudget(): Flow<Long> = budgetPrefRepo
        .getBudgetPreferenceForDateOrNext()
        .distinctUntilChanged()

    override suspend fun updateCurrentBudget(value: Long) = budgetPrefRepo.saveBudgetPreference(value)

    override fun getCurrencyPreference(): Flow<Currency> = currencyPrefRepo
        .getCurrencyPreferenceForDateOrNext()
        .distinctUntilChanged()

    override suspend fun updateCurrency(currency: Currency) = currencyPrefRepo.saveCurrency(currency)
}