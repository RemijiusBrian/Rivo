package dev.ridill.rivo.transactionFolders.presentation.transactionFoldersList

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zhuinden.flowcombinetuplekt.combineTuple
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.ridill.rivo.core.domain.model.ListMode
import dev.ridill.rivo.core.domain.util.asStateFlow
import dev.ridill.rivo.settings.domain.repositoty.SettingsRepository
import dev.ridill.rivo.transactionFolders.domain.repository.FoldersListRepository
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TxFoldersListViewModel @Inject constructor(
    private val repo: FoldersListRepository,
    settingsRepo: SettingsRepository
) : ViewModel(), TxFoldersListActions {

    private val currency = settingsRepo.getCurrencyPreference()
        .distinctUntilChanged()
    private val listMode = repo.getFoldersListMode()
        .distinctUntilChanged()
    private val folderList = repo.getFoldersWithAggregateList()

    val state = combineTuple(
        currency,
        listMode,
        folderList
    ).map { (
                currency,
                listMode,
                folderList
            ) ->
        TxFoldersListState(
            currency = currency,
            listMode = listMode,
            foldersList = folderList
        )
    }.asStateFlow(viewModelScope, TxFoldersListState())

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