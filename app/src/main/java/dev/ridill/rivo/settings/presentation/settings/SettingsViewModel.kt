package dev.ridill.rivo.settings.presentation.settings

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.zhuinden.flowcombinetuplekt.combineTuple
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.ridill.rivo.R
import dev.ridill.rivo.account.domain.repository.AuthRepository
import dev.ridill.rivo.core.data.preferences.PreferencesManager
import dev.ridill.rivo.core.domain.util.Empty
import dev.ridill.rivo.core.domain.util.EventBus
import dev.ridill.rivo.core.domain.util.asStateFlow
import dev.ridill.rivo.core.ui.util.UiText
import dev.ridill.rivo.settings.domain.modal.AppTheme
import dev.ridill.rivo.settings.domain.repositoty.SettingsRepository
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.Currency
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val repo: SettingsRepository,
    authRepo: AuthRepository,
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

    private val showTransactionAutoDetectInfo = savedStateHandle
        .getStateFlow(SHOW_AUTO_DETECT_TX_INFO, false)

    val state = combineTuple(
        authState,
        appTheme,
        dynamicColorsEnabled,
        showAppThemeSelection,
        monthlyBudget,
        showCurrencySelection,
        autoAddTransactionEnabled,
        showSmsPermissionRationale,
        showTransactionAutoDetectInfo
    ).map { (
                authState,
                appTheme,
                dynamicColorsEnabled,
                showAppThemeSelection,
                monthlyBudget,
                showCurrencySelection,
                autoAddTransactionEnabled,
                showSmsPermissionRationale,
                showTransactionAutoDetectInfo
            ) ->
        SettingsState(
            authState = authState,
            appTheme = appTheme,
            dynamicColorsEnabled = dynamicColorsEnabled,
            showAppThemeSelection = showAppThemeSelection,
            currentMonthlyBudget = monthlyBudget,
            showCurrencySelection = showCurrencySelection,
            autoAddTransactionEnabled = autoAddTransactionEnabled,
            showSmsPermissionRationale = showSmsPermissionRationale,
            showAutoDetectTransactionFeatureInfo = showTransactionAutoDetectInfo
        )
    }.asStateFlow(viewModelScope, SettingsState())

    val events = eventBus.eventFlow

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
            if (preferences.first().showAutoDetectTxInfo && enabled) {
                savedStateHandle[SHOW_AUTO_DETECT_TX_INFO] = true
            } else {
                eventBus.send(SettingsEvent.RequestSMSPermission)
            }
        }
    }

    override fun onAutoDetectTxFeatureInfoDismiss() {
        savedStateHandle[SHOW_AUTO_DETECT_TX_INFO] = false
    }

    override fun onAutoDetectTxFeatureInfoAcknowledge() {
        viewModelScope.launch {
            preferencesManager.toggleShowAutoDetectTxInfoFalse()
            savedStateHandle[SHOW_AUTO_DETECT_TX_INFO] = false
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
private const val SHOW_CURRENCY_SELECTION = "SHOW_CURRENCY_SELECTION"
private const val CURRENCY_SEARCH_QUERY = "CURRENCY_SEARCH_QUERY"
private const val SHOW_SMS_PERMISSION_RATIONALE = "SHOW_SMS_PERMISSION_RATIONALE"
private const val TEMP_AUTO_ADD_TRANSACTION_STATE = "TEMP_AUTO_ADD_TRANSACTION_STATE"
private const val SHOW_AUTO_DETECT_TX_INFO = "SHOW_AUTO_DETECT_TX_INFO"