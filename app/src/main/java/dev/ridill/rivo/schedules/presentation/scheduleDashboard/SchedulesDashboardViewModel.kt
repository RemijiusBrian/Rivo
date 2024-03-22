package dev.ridill.rivo.schedules.presentation.scheduleDashboard

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.zhuinden.flowcombinetuplekt.combineTuple
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.ridill.rivo.R
import dev.ridill.rivo.core.domain.model.Resource
import dev.ridill.rivo.core.domain.util.EventBus
import dev.ridill.rivo.core.domain.util.asStateFlow
import dev.ridill.rivo.core.ui.util.UiText
import dev.ridill.rivo.schedules.domain.model.PlanInput
import dev.ridill.rivo.schedules.domain.model.PlanListItem
import dev.ridill.rivo.schedules.domain.repository.SchedulesDashboardRepository
import dev.ridill.rivo.settings.domain.repositoty.CurrencyRepository
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SchedulesDashboardViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    currencyRepo: CurrencyRepository,
    private val repo: SchedulesDashboardRepository,
    private val eventBus: EventBus<SchedulesAndPlansListEvent>
) : ViewModel(), SchedulesDashboardActions {

    private val currency = currencyRepo.getCurrencyForDateOrNext()

    val scheduledTransactions = repo.getSchedules()
        .cachedIn(viewModelScope)

    val plansList = repo.getPlansPaged()

    private val showPlanInput = savedStateHandle.getStateFlow(SHOW_PLAN_INPUT, false)

    val planInput = savedStateHandle.getStateFlow<PlanInput?>(PLAN_INPUT, null)

    private val showDeletePlanConfirmation = savedStateHandle
        .getStateFlow(SHOW_DELETE_PLAN_CONFIRMATION, false)

    private val selectedScheduleIds = savedStateHandle
        .getStateFlow(SELECTED_SCHEDULE_IDS, emptySet<Long>())
    private val multiSelectionModeActive = selectedScheduleIds.map { it.isNotEmpty() }
        .distinctUntilChanged()
        .asStateFlow(viewModelScope, false)

    private val showDeleteSelectedSchedulesConfirmation = savedStateHandle
        .getStateFlow(SHOW_DELETE_SELECTED_SCHEDULES_CONFIRMATION, false)

    val state = combineTuple(
        currency,
        showPlanInput,
        showDeletePlanConfirmation,
        multiSelectionModeActive,
        selectedScheduleIds,
        showDeleteSelectedSchedulesConfirmation
    ).map { (
                currency,
                showPlanInput,
                showDeletePlanConfirmation,
                multiSelectionModeActive,
                selectedScheduleIds,
                showDeleteSelectedSchedulesConfirmation
            ) ->
        SchedulesDashboardState(
            currency = currency,
            showPlanInput = showPlanInput,
            showDeletePlanConfirmation = showDeletePlanConfirmation,
            multiSelectionModeActive = multiSelectionModeActive,
            selectedScheduleIds = selectedScheduleIds,
            showDeleteSelectedSchedulesConfirmation = showDeleteSelectedSchedulesConfirmation
        )
    }.asStateFlow(viewModelScope, SchedulesDashboardState())

    val events = eventBus.eventFlow

    override fun onPlanClick(plan: PlanListItem) {
        if (multiSelectionModeActive.value) {
            val selectedIds = selectedScheduleIds.value
            viewModelScope.launch {
                repo.assignSchedulesToPlan(selectedIds, plan.id)
            }
            savedStateHandle[SELECTED_SCHEDULE_IDS] = emptySet<Long>()
        } else {
            savedStateHandle[PLAN_INPUT] = PlanInput(
                id = plan.id,
                name = plan.name,
                colorCode = plan.color.toArgb(),
                createdTimestamp = plan.createdTimestamp
            )
            savedStateHandle[SHOW_PLAN_INPUT] = true
        }
    }

    override fun onNewPlanClick() {
        savedStateHandle[PLAN_INPUT] = PlanInput.INITIAL
        savedStateHandle[SHOW_PLAN_INPUT] = true
    }

    override fun onPlanInputDismiss() {
        savedStateHandle[SHOW_PLAN_INPUT] = false
        savedStateHandle[PLAN_INPUT] = null
    }

    override fun onPlanInputNameChange(value: String) {
        savedStateHandle[PLAN_INPUT] = planInput.value
            ?.copy(name = value)
    }

    override fun onPlanInputColorChange(color: Color) {
        savedStateHandle[PLAN_INPUT] = planInput.value
            ?.copy(colorCode = color.toArgb())
    }

    override fun onPlanInputConfirm() {
        val input = planInput.value ?: return
        viewModelScope.launch {
            if (input.name.trim().isEmpty()) {
                eventBus.send(SchedulesAndPlansListEvent.ShowUiMessage(UiText.StringResource(R.string.error_invalid_tag_name)))
                return@launch
            }

            repo.savePlan(input.copy(name = input.name.trim()))
            savedStateHandle[SHOW_PLAN_INPUT] = false
            savedStateHandle[PLAN_INPUT] = null
        }
    }

    override fun onDeleteActivePlanClick() {
        savedStateHandle[SHOW_PLAN_INPUT] = false
        savedStateHandle[SHOW_DELETE_PLAN_CONFIRMATION] = true
    }

    override fun onDeletePlanDismiss() {
        savedStateHandle[SHOW_DELETE_PLAN_CONFIRMATION] = false
        savedStateHandle[PLAN_INPUT] = null
    }

    override fun onDeletePlanConfirm() {
        val plan = planInput.value ?: return
        viewModelScope.launch {
            repo.deletePlan(plan)
            savedStateHandle[SHOW_DELETE_PLAN_CONFIRMATION] = false
            savedStateHandle[PLAN_INPUT] = null
        }
    }

    override fun onMarkSchedulePaidClick(id: Long) {
        viewModelScope.launch {
            when (val resource = repo.markScheduleAsPaid(id)) {
                is Resource.Error -> {
                    resource.message?.let {
                        eventBus.send(SchedulesAndPlansListEvent.ShowUiMessage(it))
                    }
                }

                is Resource.Success -> {
                    eventBus.send(SchedulesAndPlansListEvent.ShowUiMessage(UiText.StringResource(R.string.schedule_marked_as_paid)))
                }
            }
        }
    }

    override fun onScheduleLongPress(id: Long) {
        savedStateHandle[SELECTED_SCHEDULE_IDS] = selectedScheduleIds.value + id
    }

    override fun onScheduleSelectionToggle(id: Long) {
        val selectedIds = selectedScheduleIds.value
        savedStateHandle[SELECTED_SCHEDULE_IDS] = if (id in selectedIds) selectedIds - id
        else selectedIds + id
    }

    override fun onMultiSelectionModeDismiss() {
        savedStateHandle[SELECTED_SCHEDULE_IDS] = emptySet<Long>()
    }

    override fun onDeleteSelectedSchedulesClick() {
        savedStateHandle[SHOW_DELETE_SELECTED_SCHEDULES_CONFIRMATION] = true
    }

    override fun onDeleteSelectedSchedulesDismiss() {
        savedStateHandle[SHOW_DELETE_SELECTED_SCHEDULES_CONFIRMATION] = false
    }

    override fun onDeleteSelectedSchedulesConfirm() {
        viewModelScope.launch {
            repo.deleteSchedulesById(selectedScheduleIds.value)
            savedStateHandle[SHOW_DELETE_SELECTED_SCHEDULES_CONFIRMATION] = false
        }
    }

    sealed class SchedulesAndPlansListEvent {
        data class ShowUiMessage(val uiText: UiText) : SchedulesAndPlansListEvent()
    }
}

private const val SHOW_PLAN_INPUT = "SHOW_PLAN_INPUT"
private const val PLAN_INPUT = "PLAN_INPUT"
private const val SELECTED_SCHEDULE_IDS = "SELECTED_SCHEDULE_IDS"
private const val SHOW_DELETE_PLAN_CONFIRMATION = "SHOW_DELETE_PLAN_CONFIRMATION"
private const val SHOW_DELETE_SELECTED_SCHEDULES_CONFIRMATION =
    "SHOW_DELETE_SELECTED_SCHEDULES_CONFIRMATION"