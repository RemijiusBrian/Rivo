package dev.ridill.rivo.folders.presentation.foldersList

import dev.ridill.rivo.core.domain.model.SortCriteria

interface FoldersListActions {
    fun onSortOptionSelect(criteria: SortCriteria)
    fun onListModeToggle()
}