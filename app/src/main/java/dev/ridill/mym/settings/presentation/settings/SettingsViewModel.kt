package dev.ridill.mym.settings.presentation.settings

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zhuinden.flowcombinetuplekt.combineTuple
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.ridill.mym.R
import dev.ridill.mym.core.data.preferences.PreferencesManager
import dev.ridill.mym.core.domain.util.EventBus
import dev.ridill.mym.core.domain.util.TextFormat
import dev.ridill.mym.core.domain.util.Zero
import dev.ridill.mym.core.domain.util.asStateFlow
import dev.ridill.mym.core.ui.util.UiText
import dev.ridill.mym.settings.domain.modal.AppTheme
import dev.ridill.mym.settings.domain.repositoty.SettingsRepository
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val repo: SettingsRepository,
    private val preferencesManager: PreferencesManager,
    private val eventBus: EventBus<SettingsEvent>
) : ViewModel(), SettingsActions {

    private val preferences = preferencesManager.preferences
    private val appTheme = preferences.map { it.appTheme }
        .distinctUntilChanged()
    private val dynamicColorsEnabled = preferences.map { it.dynamicColorsEnabled }
        .distinctUntilChanged()

    private val monthlyBudget = repo.getCurrentBudget()
        .distinctUntilChanged()

    private val autoAddExpenseEnabled = preferences.map { it.autoAddExpenseEnabled }
        .distinctUntilChanged()

    private val showAppThemeSelection = savedStateHandle
        .getStateFlow(SHOW_APP_THEME_SELECTION, false)
    private val showMonthlyBudgetInput = savedStateHandle
        .getStateFlow(SHOW_MONTHLY_LIMIT_INPUT, false)

    private val showSmsPermissionRationale = savedStateHandle
        .getStateFlow(SHOW_SMS_PERMISSION_RATIONALE, false)

    val state = combineTuple(
        appTheme,
        dynamicColorsEnabled,
        showAppThemeSelection,
        monthlyBudget,
        showMonthlyBudgetInput,
        autoAddExpenseEnabled,
        showSmsPermissionRationale
    ).map { (
                appTheme,
                dynamicThemeEnabled,
                showAppThemeSelection,
                monthlyBudget,
                showMonthlyBudgetInput,
                autoAddExpenseEnabled,
                showSmsPermissionRationale
            ) ->
        SettingsState(
            appTheme = appTheme,
            dynamicColorsEnabled = dynamicThemeEnabled,
            showAppThemeSelection = showAppThemeSelection,
            currentMonthlyBudget = monthlyBudget.takeIf { it > Long.Zero }
                ?.let { TextFormat.currency(it) }.orEmpty(),
            showBudgetInput = showMonthlyBudgetInput,
            autoAddExpenseEnabled = autoAddExpenseEnabled,
            showSmsPermissionRationale = showSmsPermissionRationale
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

    override fun onMonthlyBudgetPreferenceClick() {
        savedStateHandle[SHOW_MONTHLY_LIMIT_INPUT] = true
    }

    override fun onMonthlyBudgetInputDismiss() {
        savedStateHandle[SHOW_MONTHLY_LIMIT_INPUT] = false
    }

    override fun onMonthlyBudgetInputConfirm(value: String) {
        viewModelScope.launch {
            val longValue = value.toLongOrNull() ?: -1L
            if (longValue <= -1L) {
                savedStateHandle[SHOW_MONTHLY_LIMIT_INPUT] = false
                eventBus.send(
                    SettingsEvent.ShowUiMessage(
                        UiText.StringResource(
                            R.string.error_invalid_amount,
                            true
                        )
                    )
                )
                return@launch
            }
            repo.updateCurrentBudget(longValue)
            savedStateHandle[SHOW_MONTHLY_LIMIT_INPUT] = false
            eventBus.send(
                SettingsEvent.ShowUiMessage(
                    UiText.StringResource(R.string.budget_updated)
                )
            )
        }
    }

    override fun onToggleAutoAddExpense(enabled: Boolean) {
        viewModelScope.launch {
            savedStateHandle[TEMP_AUTO_ADD_EXPENSE_STATE] = enabled
            eventBus.send(SettingsEvent.RequestSMSPermission)
        }
    }

    fun onSmsPermissionResult(granted: Boolean) = viewModelScope.launch {
        if (granted) {
            val enabled = savedStateHandle.get<Boolean?>(TEMP_AUTO_ADD_EXPENSE_STATE) == true
            preferencesManager.updateAutoAddExpenseEnabled(enabled)
            savedStateHandle[TEMP_AUTO_ADD_EXPENSE_STATE] = null
        }
        savedStateHandle[SHOW_SMS_PERMISSION_RATIONALE] = !granted
    }

    override fun onSmsPermissionRationaleDismiss() {
        savedStateHandle[SHOW_SMS_PERMISSION_RATIONALE] = false
    }

    override fun onSmsPermissionRationaleAgree() {
        viewModelScope.launch {
            savedStateHandle[SHOW_SMS_PERMISSION_RATIONALE] = false
            eventBus.send(SettingsEvent.RequestSMSPermission)
        }
    }

    sealed class SettingsEvent {
        data class ShowUiMessage(val uiText: UiText) : SettingsEvent()
        object RequestSMSPermission : SettingsEvent()
    }
}

private const val SHOW_APP_THEME_SELECTION = "SHOW_APP_THEME_SELECTION"
private const val SHOW_MONTHLY_LIMIT_INPUT = "SHOW_MONTHLY_LIMIT_INPUT"
private const val SHOW_SMS_PERMISSION_RATIONALE = "SHOW_SMS_PERMISSION_RATIONALE"
private const val TEMP_AUTO_ADD_EXPENSE_STATE = "TOGGLED_AUTO_ADD_EXPENSE_STATE"