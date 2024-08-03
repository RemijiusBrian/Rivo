package dev.ridill.rivo.folders.presentation.folderSelection

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.ridill.rivo.core.domain.util.Empty
import dev.ridill.rivo.folders.domain.repository.FoldersListRepository
import kotlinx.coroutines.flow.flatMapLatest
import javax.inject.Inject

@HiltViewModel
class FolderSelectionViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val repo: FoldersListRepository
) : ViewModel() {

    val searchQuery = savedStateHandle.getStateFlow(SEARCH_QUERY, String.Empty)

    val folderListPaged = searchQuery.flatMapLatest {
        repo.getFoldersListPaged(it)
    }.cachedIn(viewModelScope)

    fun onSearchQueryChange(value: String) {
        savedStateHandle[SEARCH_QUERY] = value
    }
}

private const val SEARCH_QUERY = "SEARCH_QUERY"