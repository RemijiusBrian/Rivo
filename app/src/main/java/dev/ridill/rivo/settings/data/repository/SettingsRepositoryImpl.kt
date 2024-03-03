package dev.ridill.rivo.settings.data.repository

import android.icu.util.Currency
import dev.ridill.rivo.settings.domain.repositoty.BudgetRepository
import dev.ridill.rivo.settings.domain.repositoty.CurrencyRepository
import dev.ridill.rivo.settings.domain.repositoty.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged

class SettingsRepositoryImpl(
    private val budgetRepo: BudgetRepository,
    private val currencyRepo: CurrencyRepository
) : SettingsRepository {
    override fun getCurrentBudget(): Flow<Long> = budgetRepo
        .getBudgetAmountForDateOrNext()
        .distinctUntilChanged()

    override suspend fun updateCurrentBudget(value: Long) = budgetRepo.saveBudget(value)

    override fun getCurrencyPreference(): Flow<Currency> = currencyRepo
        .getCurrencyCodeForDateOrNext()
        .distinctUntilChanged()

    override suspend fun updateCurrency(currency: Currency) = currencyRepo.saveCurrency(currency)
}