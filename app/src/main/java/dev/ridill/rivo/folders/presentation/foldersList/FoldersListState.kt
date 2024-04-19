package dev.ridill.rivo.folders.presentation.foldersList

import dev.ridill.rivo.core.domain.model.ListMode

data class FoldersListState(
    val listMode: ListMode = ListMode.GRID
)