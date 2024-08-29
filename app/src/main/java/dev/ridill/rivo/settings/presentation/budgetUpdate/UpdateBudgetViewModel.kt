package dev.ridill.rivo.settings.presentation.budgetUpdate

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.ridill.rivo.R
import dev.ridill.rivo.core.domain.util.Empty
import dev.ridill.rivo.core.domain.util.EventBus
import dev.ridill.rivo.core.ui.util.UiText
import dev.ridill.rivo.settings.domain.repositoty.BudgetPreferenceRepository
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UpdateBudgetViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val repo: BudgetPreferenceRepository,
    private val eventBus: EventBus<UpdateBudgetEvent>
) : ViewModel() {

    val currentBudget = repo.getBudgetPreferenceForDateOrNext()
        .distinctUntilChanged()

    val budgetInput = savedStateHandle
        .getStateFlow(BUDGET_INPUT, String.Empty)

    val budgetInputError = savedStateHandle
        .getStateFlow<UiText?>(BUDGET_INPUT_ERROR, null)

    val events = eventBus.eventFlow

    fun onInputChange(value: String) {
        savedStateHandle[BUDGET_INPUT] = value
        savedStateHandle[BUDGET_INPUT_ERROR] = null
    }

    fun onConfirm() = viewModelScope.launch {
        val longValue = budgetInput.value.toLongOrNull() ?: -1L
        if (longValue <= -1L) {
            savedStateHandle[BUDGET_INPUT_ERROR] = UiText.StringResource(
                R.string.error_invalid_amount,
                true
            )
            return@launch
        }
        repo.saveBudgetPreference(longValue)
        eventBus.send(UpdateBudgetEvent.BudgetUpdated)
    }

    sealed interface UpdateBudgetEvent {
        data object BudgetUpdated : UpdateBudgetEvent
    }
}

private const val BUDGET_INPUT = "BUDGET_INPUT"
private const val BUDGET_INPUT_ERROR = "BUDGET_INPUT_ERROR"