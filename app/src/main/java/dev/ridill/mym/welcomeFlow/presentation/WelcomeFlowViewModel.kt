package dev.ridill.mym.welcomeFlow.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.ridill.mym.R
import dev.ridill.mym.core.data.preferences.PreferencesManager
import dev.ridill.mym.core.domain.util.BuildUtil
import dev.ridill.mym.core.domain.util.EventBus
import dev.ridill.mym.core.domain.util.Zero
import dev.ridill.mym.core.ui.util.UiText
import dev.ridill.mym.welcomeFlow.domain.model.WelcomeFlowStop
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WelcomeFlowViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val preferenceManager: PreferencesManager,
    private val eventBus: EventBus<WelcomeFlowEvent>
) : ViewModel(), WelcomeFlowActions {

    val currentFlowStop = savedStateHandle.getStateFlow(FLOW_STOP, WelcomeFlowStop.WELCOME)
    val incomeInput = savedStateHandle.getStateFlow(INCOME_INPUT, "")
    val showNotificationRationale = savedStateHandle
        .getStateFlow(SHOW_NOTIFICATION_RATIONALE, false)

    val events = eventBus.eventFlow

    override fun onNextClick() {
        when (currentFlowStop.value) {
            WelcomeFlowStop.WELCOME -> {
                savedStateHandle[FLOW_STOP] = WelcomeFlowStop.INCOME_SET
            }

            WelcomeFlowStop.INCOME_SET -> {
                updateLimitAndContinue()
            }
        }
    }

    private fun updateLimitAndContinue() = viewModelScope.launch {
        val limitValue = incomeInput.value.toLongOrNull() ?: -1L
        if (limitValue <= Long.Zero) {
            eventBus.send(
                WelcomeFlowEvent.ShowUiMessage(
                    UiText.StringResource(R.string.error_invalid_amount, true)
                )
            )
            return@launch
        }
        preferenceManager.updateMonthlyLimit(limitValue)
        if (BuildUtil.isNotificationRuntimePermissionNeeded()) {
            savedStateHandle[SHOW_NOTIFICATION_RATIONALE] = true
        } else {
            concludeWelcomeFlow()
        }
    }

    private fun concludeWelcomeFlow() = viewModelScope.launch {
        preferenceManager.concludeWelcomeFlow()
        eventBus.send(WelcomeFlowEvent.WelcomeFlowConcluded)
    }

    override fun onIncomeInputChange(value: String) {
        savedStateHandle[INCOME_INPUT] = value
    }

    override fun onNotificationRationaleDismiss() {
        savedStateHandle[SHOW_NOTIFICATION_RATIONALE] = false
        concludeWelcomeFlow()
    }

    override fun onNotificationRationaleAgree() {
        viewModelScope.launch {
            savedStateHandle[SHOW_NOTIFICATION_RATIONALE] = false
            eventBus.send(WelcomeFlowEvent.RequestPermissionRequest)
        }
    }

    override fun onPermissionResponse() {
        concludeWelcomeFlow()
    }

    sealed class WelcomeFlowEvent {
        object WelcomeFlowConcluded : WelcomeFlowEvent()
        data class ShowUiMessage(val uiText: UiText) : WelcomeFlowEvent()
        object RequestPermissionRequest : WelcomeFlowEvent()
    }
}

private const val FLOW_STOP = "FLOW_STOP"
private const val INCOME_INPUT = "INCOME_INPUT"
private const val SHOW_NOTIFICATION_RATIONALE = "SHOW_NOTIFICATION_RATIONALE"