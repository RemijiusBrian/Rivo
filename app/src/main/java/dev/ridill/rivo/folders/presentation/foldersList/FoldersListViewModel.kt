package dev.ridill.rivo.folders.presentation.foldersList

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.zhuinden.flowcombinetuplekt.combineTuple
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.ridill.rivo.core.domain.util.asStateFlow
import dev.ridill.rivo.folders.domain.repository.FoldersListRepository
import dev.ridill.rivo.settings.domain.repositoty.CurrencyRepository
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FoldersListViewModel @Inject constructor(
    private val repo: FoldersListRepository,
    currencyRepo: CurrencyRepository
) : ViewModel(), FoldersListActions {
    private val currency = currencyRepo
        .getCurrencyCodeForDateOrNext()
        .distinctUntilChanged()
    private val listMode = repo.getFoldersListMode()

    val folderListPagingData = repo.getFoldersWithAggregateList()
        .cachedIn(viewModelScope)

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
            val newListMode = !state.value.listMode
            repo.updateFoldersListMode(newListMode)
        }
    }
}

const val ACTION_FOLDER_DETAILS = "ACTION_FOLDER_DETAILS"