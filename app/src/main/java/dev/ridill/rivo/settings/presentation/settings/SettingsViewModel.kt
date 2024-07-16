package dev.ridill.rivo.settings.presentation.settings

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.zhuinden.flowcombinetuplekt.combineTuple
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.ridill.rivo.R
import dev.ridill.rivo.core.data.preferences.PreferencesManager
import dev.ridill.rivo.core.domain.util.Empty
import dev.ridill.rivo.core.domain.util.EventBus
import dev.ridill.rivo.core.domain.util.asStateFlow
import dev.ridill.rivo.core.ui.util.UiText
import dev.ridill.rivo.settings.domain.modal.AppTheme
import dev.ridill.rivo.settings.domain.repositoty.AuthRepository
import dev.ridill.rivo.settings.domain.repositoty.SettingsRepository
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.Currency
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val repo: SettingsRepository,
    private val authRepo: AuthRepository,
    private val preferencesManager: PreferencesManager,
    private val eventBus: EventBus<SettingsEvent>
) : ViewModel(), SettingsActions {

    private val authState = authRepo.getAuthState()

    private val preferences = preferencesManager.preferences
    private val appTheme = preferences.map { it.appTheme }
        .distinctUntilChanged()
    private val dynamicColorsEnabled = preferences.map { it.dynamicColorsEnabled }
        .distinctUntilChanged()

    private val monthlyBudget = repo.getCurrentBudget()
        .distinctUntilChanged()

    private val autoAddTransactionEnabled = preferences.map { it.transactionAutoDetectEnabled }
        .distinctUntilChanged()

    private val showAppThemeSelection = savedStateHandle
        .getStateFlow(SHOW_APP_THEME_SELECTION, false)

    private val showMonthlyBudgetInput = savedStateHandle
        .getStateFlow(SHOW_MONTHLY_BUDGET_INPUT, false)
    private val budgetInputError = savedStateHandle
        .getStateFlow<UiText?>(BUDGET_INPUT_ERROR, null)

    private val showCurrencySelection = savedStateHandle
        .getStateFlow(SHOW_CURRENCY_SELECTION, false)
    val currencySearchQuery = savedStateHandle
        .getStateFlow(CURRENCY_SEARCH_QUERY, "")

    val currenciesPagingData = currencySearchQuery
        .flatMapLatest { query ->
            repo.getCurrenciesListPaged(query)
        }.cachedIn(viewModelScope)


    private val showSmsPermissionRationale = savedStateHandle
        .getStateFlow(SHOW_SMS_PERMISSION_RATIONALE, false)

    private val showLogoutConfirmation = savedStateHandle
        .getStateFlow(SHOW_LOGOUT_CONFIRMATION, false)

    val state = combineTuple(
        authState,
        showLogoutConfirmation,
        appTheme,
        dynamicColorsEnabled,
        showAppThemeSelection,
        monthlyBudget,
        budgetInputError,
        showMonthlyBudgetInput,
        showCurrencySelection,
        autoAddTransactionEnabled,
        showSmsPermissionRationale
    ).map { (
                authState,
                showLogoutConfirmation,
                appTheme,
                dynamicColorsEnabled,
                showAppThemeSelection,
                monthlyBudget,
                budgetInputError,
                showMonthlyBudgetInput,
                showCurrencySelection,
                autoAddTransactionEnabled,
                showSmsPermissionRationale
            ) ->
        SettingsState(
            authState = authState,
            showLogoutConfirmation = showLogoutConfirmation,
            appTheme = appTheme,
            dynamicColorsEnabled = dynamicColorsEnabled,
            showAppThemeSelection = showAppThemeSelection,
            currentMonthlyBudget = monthlyBudget,
            showBudgetInput = showMonthlyBudgetInput,
            budgetInputError = budgetInputError,
            showCurrencySelection = showCurrencySelection,
            autoAddTransactionEnabled = autoAddTransactionEnabled,
            showSmsPermissionRationale = showSmsPermissionRationale
        )
    }.asStateFlow(viewModelScope, SettingsState())

    val events = eventBus.eventFlow

    override fun onLogoutClick() {
        savedStateHandle[SHOW_LOGOUT_CONFIRMATION] = true
    }

    override fun onLogoutDismiss() {
        savedStateHandle[SHOW_LOGOUT_CONFIRMATION] = false
    }

    override fun onLogoutConfirm() {
        viewModelScope.launch {
            authRepo.signUserOut()
            savedStateHandle[SHOW_LOGOUT_CONFIRMATION] = false
        }
    }

    override fun onAppThemePreferenceClick() {
        savedStateHandle[SHOW_APP_THEME_SELECTION] = true
    }

    override fun onAppThemeSelectionDismiss() {
        savedStateHandle[SHOW_APP_THEME_SELECTION] = false
    }

    override fun onAppThemeSelectionConfirm(appTheme: AppTheme) {
        viewModelScope.launch {
            savedStateHandle[SHOW_APP_THEME_SELECTION] = false
            preferencesManager.updateAppThem(appTheme)
        }
    }

    override fun onDynamicThemeEnabledChange(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.updateDynamicColorsEnabled(enabled)
        }
    }

    override fun onMonthlyBudgetPreferenceClick() {
        savedStateHandle[SHOW_MONTHLY_BUDGET_INPUT] = true
    }

    override fun onMonthlyBudgetInputDismiss() {
        savedStateHandle[SHOW_MONTHLY_BUDGET_INPUT] = false
    }

    override fun onMonthlyBudgetInputConfirm(value: String) {
        viewModelScope.launch {
            val longValue = value.toLongOrNull() ?: -1L
            if (longValue <= -1L) {
                savedStateHandle[BUDGET_INPUT_ERROR] = UiText.StringResource(
                    R.string.error_invalid_amount,
                    true
                )
                return@launch
            }
            repo.updateCurrentBudget(longValue)
            savedStateHandle[SHOW_MONTHLY_BUDGET_INPUT] = false
            eventBus.send(
                SettingsEvent.ShowUiMessage(
                    UiText.StringResource(R.string.budget_updated)
                )
            )
        }
    }

    override fun onCurrencyPreferenceClick() {
        savedStateHandle[SHOW_CURRENCY_SELECTION] = true
    }

    override fun onCurrencySelectionDismiss() {
        clearAndDismissCurrencySelection()
    }

    override fun onCurrencySelectionConfirm(currency: Currency) {
        viewModelScope.launch {
            repo.updateCurrency(currency)
            clearAndDismissCurrencySelection()
            eventBus.send(SettingsEvent.ShowUiMessage(UiText.StringResource(R.string.currency_updated)))
        }
    }

    override fun onCurrencySearchQueryChange(value: String) {
        savedStateHandle[CURRENCY_SEARCH_QUERY] = value
    }

    private fun clearAndDismissCurrencySelection() {
        savedStateHandle[SHOW_CURRENCY_SELECTION] = false
        savedStateHandle[CURRENCY_SEARCH_QUERY] = String.Empty
    }

    override fun onToggleAutoAddTransactions(enabled: Boolean) {
        viewModelScope.launch {
            savedStateHandle[TEMP_AUTO_ADD_TRANSACTION_STATE] = enabled
            eventBus.send(SettingsEvent.RequestSMSPermission)
        }
    }

    fun onSmsPermissionResult(granted: Boolean) = viewModelScope.launch {
        if (granted) {
            val enabled = savedStateHandle.get<Boolean?>(TEMP_AUTO_ADD_TRANSACTION_STATE) == true
            preferencesManager.updateTransactionAutoDetectEnabled(enabled)
            savedStateHandle[TEMP_AUTO_ADD_TRANSACTION_STATE] = null
        }
        savedStateHandle[SHOW_SMS_PERMISSION_RATIONALE] = !granted
    }

    override fun onSmsPermissionRationaleDismiss() {
        savedStateHandle[SHOW_SMS_PERMISSION_RATIONALE] = false
    }

    override fun onSmsPermissionRationaleSettingsClick() {
        viewModelScope.launch {
            savedStateHandle[SHOW_SMS_PERMISSION_RATIONALE] = false
            eventBus.send(SettingsEvent.LaunchAppSettings)
        }
    }

    sealed interface SettingsEvent {
        data class ShowUiMessage(val uiText: UiText) : SettingsEvent
        data object RequestSMSPermission : SettingsEvent
        data object LaunchAppSettings : SettingsEvent
    }
}

private const val SHOW_APP_THEME_SELECTION = "SHOW_APP_THEME_SELECTION"
private const val SHOW_MONTHLY_BUDGET_INPUT = "SHOW_MONTHLY_BUDGET_INPUT"
private const val SHOW_CURRENCY_SELECTION = "SHOW_CURRENCY_SELECTION"
private const val CURRENCY_SEARCH_QUERY = "CURRENCY_SEARCH_QUERY"
private const val BUDGET_INPUT_ERROR = "BUDGET_INPUT_ERROR"
private const val SHOW_SMS_PERMISSION_RATIONALE = "SHOW_SMS_PERMISSION_RATIONALE"
private const val TEMP_AUTO_ADD_TRANSACTION_STATE = "TEMP_AUTO_ADD_TRANSACTION_STATE"
private const val SHOW_LOGOUT_CONFIRMATION = "SHOW_LOGOUT_CONFIRMATION"