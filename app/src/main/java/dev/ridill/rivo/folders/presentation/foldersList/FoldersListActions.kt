package dev.ridill.rivo.folders.presentation.foldersList

import dev.ridill.rivo.folders.domain.model.FolderSortCriteria
import dev.ridill.rivo.folders.domain.model.FoldersListOption

interface FoldersListActions {
    fun onSortOptionSelect(criteria: FolderSortCriteria)
    fun onListModeToggle()
    fun onListOptionSelect(option: FoldersListOption)
}