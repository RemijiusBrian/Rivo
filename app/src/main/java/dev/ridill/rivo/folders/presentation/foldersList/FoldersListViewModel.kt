package dev.ridill.rivo.folders.presentation.foldersList

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zhuinden.flowcombinetuplekt.combineTuple
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.ridill.rivo.core.domain.model.ListMode
import dev.ridill.rivo.core.domain.util.asStateFlow
import dev.ridill.rivo.settings.domain.repositoty.SettingsRepository
import dev.ridill.rivo.folders.domain.repository.FoldersListRepository
import kotlinx.coroutines.flow.distinctUntilChanged
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
    private val listMode = repo.getFoldersListMode()
        .distinctUntilChanged()
    val folderListPagingData = repo.getFoldersWithAggregateList()

    val state = combineTuple(
        currency,
        listMode
    ).map { (
                currency,
                listMode
            ) ->
        FoldersListState(
            currency = currency,
            listMode = listMode
        )
    }.asStateFlow(viewModelScope, FoldersListState())

    override fun onListModeToggle() {
        viewModelScope.launch {
            val newListMode = when (state.value.listMode) {
                ListMode.LIST -> ListMode.GRID
                ListMode.GRID -> ListMode.LIST
            }
            repo.updateFoldersListMode(newListMode)
        }
    }
}

const val ACTION_FOLDER_DETAILS = "ACTION_FOLDER_DETAILS"