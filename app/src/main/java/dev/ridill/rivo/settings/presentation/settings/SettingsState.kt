package dev.ridill.rivo.settings.presentation.settings

import android.icu.util.Currency
import dev.ridill.rivo.core.domain.util.CurrencyUtil
import dev.ridill.rivo.core.ui.util.UiText
import dev.ridill.rivo.settings.domain.modal.AppTheme

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