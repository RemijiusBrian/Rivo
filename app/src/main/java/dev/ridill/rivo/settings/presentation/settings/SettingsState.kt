package dev.ridill.rivo.settings.presentation.settings

import dev.ridill.rivo.account.domain.model.AuthState
import dev.ridill.rivo.core.domain.util.Zero
import dev.ridill.rivo.core.ui.util.UiText
import dev.ridill.rivo.settings.domain.modal.AppTheme

data class SettingsState(
    val authState: AuthState = AuthState.UnAuthenticated,
    val showLogoutConfirmation: Boolean = false,
    val appTheme: AppTheme = AppTheme.SYSTEM_DEFAULT,
    val dynamicColorsEnabled: Boolean = false,
    val showAppThemeSelection: Boolean = false,
    val currentMonthlyBudget: Long = Long.Zero,
    val showBudgetInput: Boolean = false,
    val budgetInputError: UiText? = null,
    val showCurrencySelection: Boolean = false,
    val autoAddTransactionEnabled: Boolean = false,
    val showSmsPermissionRationale: Boolean = false,
    val showAutoDetectTransactionFeatureInfo: Boolean = false
)