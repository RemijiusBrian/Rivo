package dev.ridill.rivo.transactionSchedules.presentation.schedulesAndPlansList

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.ridill.rivo.R
import dev.ridill.rivo.core.domain.model.Resource
import dev.ridill.rivo.core.domain.util.EventBus
import dev.ridill.rivo.core.ui.util.UiText
import dev.ridill.rivo.transactionSchedules.domain.repository.SchedulesAndPlansRepository
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SchedulesAndPlansListViewModel @Inject constructor(
    private val repo: SchedulesAndPlansRepository,
    private val eventBus: EventBus<SchedulesAndPlansListEvent>
) : ViewModel(), SchedulesAndPlansActions {

    val scheduledTransactions = repo.getSchedules()
        .cachedIn(viewModelScope)

    val events = eventBus.eventFlow

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
}

sealed class SchedulesAndPlansListEvent {
    data class ShowUiMessage(val uiText: UiText) : SchedulesAndPlansListEvent()
}