package dev.ridill.rivo.folders.presentation.folderSelection

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.ridill.rivo.core.domain.util.Empty
import dev.ridill.rivo.core.ui.navigation.destinations.FolderSelectionSheetSpec
import dev.ridill.rivo.folders.domain.repository.AllFoldersRepository
import kotlinx.coroutines.flow.flatMapLatest
import javax.inject.Inject

@HiltViewModel
class FolderSelectionViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val repo: AllFoldersRepository
) : ViewModel() {

    val searchQuery = savedStateHandle.getStateFlow(SEARCH_QUERY, String.Empty)

    val selectedFolderId = savedStateHandle
        .getStateFlow<Long?>(SELECTED_FOLDER_ID, null)

    val folderListPaged = searchQuery.flatMapLatest {
        repo.getFoldersListPaged(it)
    }.cachedIn(viewModelScope)

    init {
        savedStateHandle[SELECTED_FOLDER_ID] = FolderSelectionSheetSpec
            .getPreselectedIdFromSavedStateHandle(savedStateHandle)
    }

    fun onSearchQueryChange(value: String) {
        savedStateHandle[SEARCH_QUERY] = value
    }

    fun onFolderSelect(folderId: Long) {
        savedStateHandle[SELECTED_FOLDER_ID] = folderId
            .takeIf { it != selectedFolderId.value }
    }
}

private const val SEARCH_QUERY = "SEARCH_QUERY"
private const val SELECTED_FOLDER_ID = "SELECTED_FOLDER_ID"