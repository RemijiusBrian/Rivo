package dev.ridill.rivo.settings.presentation.settings

import dev.ridill.rivo.core.domain.util.LocaleUtil
import dev.ridill.rivo.core.ui.util.UiText
import dev.ridill.rivo.settings.domain.modal.AppTheme
import java.util.Currency

data class SettingsState(
    val appTheme: AppTheme = AppTheme.SYSTEM_DEFAULT,
    val dynamicColorsEnabled: Boolean = false,
    val showAppThemeSelection: Boolean = false,
    val currentMonthlyBudget: String = "",
    val showBudgetInput: Boolean = false,
    val budgetInputError: UiText? = null,
    val currentCurrency: Currency = LocaleUtil.defaultCurrency,
    val showCurrencySelection: Boolean = false,
    val currencyList: List<Currency> = emptyList(),
    val autoAddTransactionEnabled: Boolean = false,
    val showSmsPermissionRationale: Boolean = false
)