package dev.ridill.rivo.folders.presentation.foldersList

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.ridill.rivo.folders.domain.repository.FoldersListRepository
import javax.inject.Inject

@HiltViewModel
class FoldersListViewModel @Inject constructor(
    repo: FoldersListRepository,
) : ViewModel() {
    val folderListPagingData = repo.getFoldersWithAggregateList()
        .cachedIn(viewModelScope)
}