package dev.ridill.rivo.transactionGroups.presentation.groupsList

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zhuinden.flowcombinetuplekt.combineTuple
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.ridill.rivo.core.domain.model.ListMode
import dev.ridill.rivo.core.domain.util.asStateFlow
import dev.ridill.rivo.settings.domain.repositoty.SettingsRepository
import dev.ridill.rivo.transactionGroups.domain.repository.TxGroupsListRepository
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TxGroupsListViewModel @Inject constructor(
    private val txGroupsRepo: TxGroupsListRepository,
    settingsRepo: SettingsRepository
) : ViewModel(), TxGroupsListActions {

    private val currency = settingsRepo.getCurrencyPreference()
        .distinctUntilChanged()
    private val listMode = txGroupsRepo.getGroupsListMode()
        .distinctUntilChanged()
    private val groupsList = txGroupsRepo.getGroupsList()

    val state = combineTuple(
        currency,
        listMode,
        groupsList
    ).map { (
                currency,
                listMode,
                groupsList
            ) ->
        TxGroupsListState(
            currency = currency,
            listMode = listMode,
            groupsList = groupsList
        )
    }.asStateFlow(viewModelScope, TxGroupsListState())

    override fun onListModeToggle() {
        viewModelScope.launch {
            val newListMode = when (state.value.listMode) {
                ListMode.LIST -> ListMode.GRID
                ListMode.GRID -> ListMode.LIST
            }
            txGroupsRepo.updateGroupsListMode(newListMode)
        }
    }
}