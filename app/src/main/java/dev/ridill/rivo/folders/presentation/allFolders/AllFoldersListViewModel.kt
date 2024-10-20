package dev.ridill.rivo.folders.presentation.allFolders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.ridill.rivo.folders.domain.repository.AllFoldersRepository
import javax.inject.Inject

@HiltViewModel
class AllFoldersListViewModel @Inject constructor(
    repo: AllFoldersRepository,
) : ViewModel() {
    val folderListPagingData = repo.getFolderAndAggregatePaged()
        .cachedIn(viewModelScope)
}