package dev.ridill.rivo.folders.presentation.foldersList

import dev.ridill.rivo.folders.domain.model.FolderSortCriteria

interface FoldersListActions {
    fun onSortOptionSelect(criteria: FolderSortCriteria)
    fun onListModeToggle()
}