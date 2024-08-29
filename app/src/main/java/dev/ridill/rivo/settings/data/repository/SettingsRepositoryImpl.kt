package dev.ridill.rivo.settings.data.repository

import dev.ridill.rivo.account.domain.model.AuthState
import dev.ridill.rivo.account.domain.repository.AuthRepository
import dev.ridill.rivo.core.data.preferences.PreferencesManager
import dev.ridill.rivo.core.domain.model.RivoPreferences
import dev.ridill.rivo.settings.domain.modal.AppTheme
import dev.ridill.rivo.settings.domain.repositoty.BudgetPreferenceRepository
import dev.ridill.rivo.settings.domain.repositoty.CurrencyPreferenceRepository
import dev.ridill.rivo.settings.domain.repositoty.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import java.util.Currency

class SettingsRepositoryImpl(
    private val authRepo: AuthRepository,
    private val preferencesManager: PreferencesManager,
    private val budgetPrefRepo: BudgetPreferenceRepository,
    private val currencyPrefRepo: CurrencyPreferenceRepository
) : SettingsRepository {
    override fun getCurrentBudget(): Flow<Long> = budgetPrefRepo
        .getBudgetPreferenceForDateOrNext()
        .distinctUntilChanged()

    override fun getCurrencyPreference(): Flow<Currency> = currencyPrefRepo
        .getCurrencyPreferenceForDateOrNext()
        .distinctUntilChanged()

    override fun getAuthState(): Flow<AuthState> = authRepo.getAuthState()

    override fun getPreferences(): Flow<RivoPreferences> = preferencesManager.preferences

    override suspend fun updateAppTheme(theme: AppTheme) = preferencesManager.updateAppThem(theme)

    override suspend fun toggleDynamicColors(enabled: Boolean) =
        preferencesManager.updateDynamicColorsEnabled(enabled)

    override suspend fun toggleAutoDetectTransactions(enabled: Boolean) =
        preferencesManager.updateTransactionAutoDetectEnabled(enabled)

    override suspend fun toggleShowAutoDetectTxInfoFalse() =
        preferencesManager.toggleShowAutoDetectTxInfoFalse()
}