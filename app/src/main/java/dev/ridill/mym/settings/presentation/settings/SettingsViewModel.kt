package dev.ridill.mym.settings.presentation.settings

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zhuinden.flowcombinetuplekt.combineTuple
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.ridill.mym.R
import dev.ridill.mym.core.data.preferences.PreferencesManager
import dev.ridill.mym.core.domain.service.AppDistributionService
import dev.ridill.mym.core.domain.util.EventBus
import dev.ridill.mym.core.domain.util.TextFormatUtil
import dev.ridill.mym.core.domain.util.Zero
import dev.ridill.mym.core.domain.util.asStateFlow
import dev.ridill.mym.core.ui.util.UiText
import dev.ridill.mym.settings.domain.modal.AppTheme
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val preferencesManager: PreferencesManager,
    private val eventBus: EventBus<SettingsEvent>,
    private val appDistributionService: AppDistributionService
) : ViewModel(), SettingsActions {

    private val preferences = preferencesManager.preferences
    private val appTheme = preferences.map { it.appTheme }
        .distinctUntilChanged()
    private val dynamicColorsEnabled = preferences.map { it.dynamicColorsEnabled }
        .distinctUntilChanged()
    private val monthlyLimit = preferences.map { it.monthlyLimit }
        .distinctUntilChanged()

    private val showAppThemeSelection = savedStateHandle
        .getStateFlow(SHOW_APP_THEME_SELECTION, false)
    private val showMonthlyLimitInput = savedStateHandle
        .getStateFlow(SHOW_MONTHLY_LIMIT_INPUT, false)

    private val showSmsPermissionRationale = savedStateHandle
        .getStateFlow(SHOW_SMS_PERMISSION_RATIONALE, false)

    val state = combineTuple(
        appTheme,
        dynamicColorsEnabled,
        showAppThemeSelection,
        monthlyLimit,
        showMonthlyLimitInput,
        showSmsPermissionRationale
    ).map { (
                appTheme,
                dynamicThemeEnabled,
                showAppThemeSelection,
                monthlyLimit,
                showMonthlyLimitInput,
                showSmsPermissionRationale
            ) ->
        SettingsState(
            appTheme = appTheme,
            dynamicColorsEnabled = dynamicThemeEnabled,
            showAppThemeSelection = showAppThemeSelection,
            currentMonthlyLimit = monthlyLimit.takeIf { it > Long.Zero }
                ?.let { TextFormatUtil.currency(it) }.orEmpty(),
            showMonthlyLimitInput = showMonthlyLimitInput,
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

    override fun onMonthlyLimitPreferenceClick() {
        savedStateHandle[SHOW_MONTHLY_LIMIT_INPUT] = true
    }

    override fun onMonthlyLimitInputDismiss() {
        savedStateHandle[SHOW_MONTHLY_LIMIT_INPUT] = false
    }

    override fun onMonthlyLimitInputConfirm(value: String) {
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
            preferencesManager.updateMonthlyLimit(longValue)
            savedStateHandle[SHOW_MONTHLY_LIMIT_INPUT] = false
            eventBus.send(
                SettingsEvent.ShowUiMessage(
                    UiText.StringResource(R.string.income_updated)
                )
            )
        }
    }

    override fun onAutoAddExpensePreferenceClick() {
        savedStateHandle[SHOW_SMS_PERMISSION_RATIONALE] = true
    }

    override fun onSmsPermissionRationaleDismiss() {
        savedStateHandle[SHOW_SMS_PERMISSION_RATIONALE] = false
    }

    override fun onSmsPermissionRationaleAgree() {
        viewModelScope.launch {
            savedStateHandle[SHOW_SMS_PERMISSION_RATIONALE] = false
            eventBus.send(SettingsEvent.RequestSmsPermission)
        }
    }

    override fun onFeedbackPreferenceClick() {
        viewModelScope.launch {
            if (appDistributionService.isTesterSignedIn) {
                appDistributionService.startFeedback()
            } else {
                val message = appDistributionService.enableTestingFeatures()
                eventBus.send(SettingsEvent.ShowUiMessage(message))
            }
        }
    }

    sealed class SettingsEvent {
        data class ShowUiMessage(val uiText: UiText) : SettingsEvent()
        object RequestSmsPermission : SettingsEvent()
    }
}

private const val SHOW_APP_THEME_SELECTION = "SHOW_APP_THEME_SELECTION"
private const val SHOW_MONTHLY_LIMIT_INPUT = "SHOW_MONTHLY_LIMIT_INPUT"
private const val SHOW_SMS_PERMISSION_RATIONALE = "SHOW_SMS_PERMISSION_RATIONALE"