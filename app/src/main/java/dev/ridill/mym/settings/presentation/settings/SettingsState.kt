package dev.ridill.mym.settings.presentation.settings

import dev.ridill.mym.core.domain.util.CurrencyUtil
import dev.ridill.mym.core.ui.util.UiText
import dev.ridill.mym.settings.domain.modal.AppTheme
import java.util.Currency

data class SettingsState(
    val appTheme: AppTheme = AppTheme.SYSTEM_DEFAULT,
    val dynamicColorsEnabled: Boolean = false,
    val showAppThemeSelection: Boolean = false,
    val currentMonthlyBudget: String = "",
    val showBudgetInput: Boolean = false,
    val budgetInputError: UiText? = null,
    val currentCurrency: Currency = CurrencyUtil.default,
    val showCurrencySelection: Boolean = false,
    val currencyList: List<Currency> = emptyList(),
    val autoAddExpenseEnabled: Boolean = false,
    val showSmsPermissionRationale: Boolean = false
)