package dev.ridill.rivo.tags.presentation.allTags

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.ridill.rivo.core.domain.util.Empty
import dev.ridill.rivo.core.domain.util.UtilConstants
import dev.ridill.rivo.tags.domain.repository.TagsRepository
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import javax.inject.Inject

@HiltViewModel
class AllTagsViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val repo: TagsRepository
) : ViewModel(), AllTagsActions {

    val searchQuery = savedStateHandle.getStateFlow(SEARCH_QUERY, String.Empty)
    val allTagsPagingData = searchQuery
        .debounce(UtilConstants.DEBOUNCE_TIMEOUT)
        .flatMapLatest {
            repo.getAllTagsPagingData(it)
        }.cachedIn(viewModelScope)

    override fun onSearchQueryChange(value: String) {
        savedStateHandle[SEARCH_QUERY] = value
    }
}

private const val SEARCH_QUERY = "SEARCH_QUERY"