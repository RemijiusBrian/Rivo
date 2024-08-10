package dev.ridill.rivo.tags.presentation.tagSelection

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.ridill.rivo.core.domain.util.Empty
import dev.ridill.rivo.core.domain.util.UtilConstants
import dev.ridill.rivo.core.ui.navigation.destinations.TagSelectionSheetSpec
import dev.ridill.rivo.tags.domain.repository.TagsRepository
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import javax.inject.Inject

@HiltViewModel
class TagSelectionViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    repo: TagsRepository
) : ViewModel() {

    private val multiSelection = TagSelectionSheetSpec
        .getMultiSelectionFromSavedStateHandle(savedStateHandle)

    val searchQuery = savedStateHandle
        .getStateFlow(SEARCH_QUERY, String.Empty)

    val selectedIds = savedStateHandle
        .getStateFlow<Set<Long>>(SELECTED_IDS, emptySet())

    val tagsPagingData = searchQuery
        .debounce(UtilConstants.DEBOUNCE_TIMEOUT)
        .flatMapLatest {
            repo.getAllTagsPagingData(it)
        }.cachedIn(viewModelScope)

    fun onSearchQueryChange(value: String) {
        savedStateHandle[SEARCH_QUERY] = value
    }

    fun onItemClick(id: Long) {
        if (multiSelection) {
            val currentIds = selectedIds.value
            savedStateHandle[SELECTED_IDS] = if (id in currentIds) currentIds - id
            else currentIds + id
        } else {
            savedStateHandle[SELECTED_IDS] = setOf(id)
        }
    }
}

private const val SEARCH_QUERY = "SEARCH_QUERY"
private const val SELECTED_IDS = "SELECTED_IDS"