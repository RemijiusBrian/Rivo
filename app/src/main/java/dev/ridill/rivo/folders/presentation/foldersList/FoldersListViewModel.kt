package dev.ridill.rivo.folders.presentation.foldersList

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.zhuinden.flowcombinetuplekt.combineTuple
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.ridill.rivo.core.domain.util.asStateFlow
import dev.ridill.rivo.folders.domain.model.FolderSortCriteria
import dev.ridill.rivo.folders.domain.model.FoldersListOption
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
    private val showBalancedFolders = repo.getShowBalancedFolders()
        .distinctUntilChanged()

    val folderListPagingData = combineTuple(
        sortCriteria,
        sortOrder,
        showBalancedFolders
    ).flatMapLatest { (sortCriteria, sortOrder, showBalanced) ->
        repo.getFoldersWithAggregateList(
            sortCriteria = sortCriteria,
            sortOrder = sortOrder,
            showBalanced = showBalanced
        )
    }.cachedIn(viewModelScope)

    val state = combineTuple(
        currency,
        sortCriteria,
        sortOrder,
        listMode,
        showBalancedFolders
    ).map { (
                currency,
                sortCriteria,
                sortOrder,
                listMode,
                showBalancedFolders
            ) ->
        FoldersListState(
            currency = currency,
            sortCriteria = sortCriteria,
            sortOrder = sortOrder,
            listMode = listMode,
            showBalancedFolders = showBalancedFolders
        )
    }.asStateFlow(viewModelScope, FoldersListState())

    override fun onSortOptionSelect(criteria: FolderSortCriteria) {
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

    override fun onListOptionSelect(option: FoldersListOption) {
        viewModelScope.launch {
            when (option) {
                FoldersListOption.SHOW_HIDE_BALANCED -> {
                    repo.toggleShowBalancedFolders()
                }
            }
        }
    }
}

const val ACTION_FOLDER_DETAILS = "ACTION_FOLDER_DETAILS"