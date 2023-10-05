package dev.ridill.rivo.folders.presentation.foldersList

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zhuinden.flowcombinetuplekt.combineTuple
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.ridill.rivo.core.domain.model.SortCriteria
import dev.ridill.rivo.core.domain.util.asStateFlow
import dev.ridill.rivo.folders.domain.repository.FoldersListRepository
import dev.ridill.rivo.settings.domain.repositoty.SettingsRepository
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FoldersListViewModel @Inject constructor(
    private val repo: FoldersListRepository,
    settingsRepo: SettingsRepository
) : ViewModel(), FoldersListActions {

    private val currency = settingsRepo.getCurrencyPreference()
        .distinctUntilChanged()
    private val sortCriteria = repo.getFoldersListSortCriteria()
        .distinctUntilChanged()
    private val sortOrder = repo.getFoldersListSortOrder()
        .distinctUntilChanged()
    private val listMode = repo.getFoldersListMode()
        .distinctUntilChanged()

    val folderListPagingData = combineTuple(
        sortCriteria,
        sortOrder
    ).flatMapLatest { (sortCriteria, sortOrder) ->
        repo.getFoldersWithAggregateList(
            sortCriteria = sortCriteria,
            sortOrder = sortOrder
        )
    }

    val state = combineTuple(
        currency,
        sortCriteria,
        sortOrder,
        listMode
    ).map { (
                currency,
                sortCriteria,
                sortOrder,
                listMode
            ) ->
        FoldersListState(
            currency = currency,
            sortCriteria = sortCriteria,
            sortOrder = sortOrder,
            listMode = listMode
        )
    }.asStateFlow(viewModelScope, FoldersListState())

    override fun onSortOptionSelect(criteria: SortCriteria) {
        viewModelScope.launch {
            val state = state.value
            val sortOrder = if (criteria == state.sortCriteria) !state.sortOrder
            else state.sortOrder

            repo.updateFoldersListSort(criteria, sortOrder)
        }
    }

    override fun onListModeToggle() {
        viewModelScope.launch {
            val newListMode = !state.value.listMode
            repo.updateFoldersListMode(newListMode)
        }
    }
}

const val ACTION_FOLDER_DETAILS = "ACTION_FOLDER_DETAILS"