package dev.ridill.rivo.transactionGroups.presentation.groupsList

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.ridill.rivo.core.domain.util.EventBus
import dev.ridill.rivo.core.ui.util.UiText
import dev.ridill.rivo.transactionGroups.domain.repository.TxGroupsListRepository
import javax.inject.Inject

@HiltViewModel
class TxGroupsListViewModel @Inject constructor(
    private val repo: TxGroupsListRepository,
    private val eventBus: EventBus<TxGroupsListEvent>
) : ViewModel() {

    val groupsList = repo.getGroupsList()

    val events = eventBus.eventFlow

    sealed class TxGroupsListEvent {
        data class ShowUiMessage(val uiText: UiText) : TxGroupsListEvent()
    }
}