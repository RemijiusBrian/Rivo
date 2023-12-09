package dev.ridill.rivo.folders.presentation.foldersList

import android.icu.util.Currency
import dev.ridill.rivo.core.domain.model.ListMode
import dev.ridill.rivo.core.domain.model.SortOrder
import dev.ridill.rivo.core.domain.util.LocaleUtil
import dev.ridill.rivo.folders.domain.model.FolderSortCriteria

data class FoldersListState(
    val currency: Currency = LocaleUtil.defaultCurrency,
    val sortCriteria: FolderSortCriteria = FolderSortCriteria.BY_AGGREGATE,
    val sortOrder: SortOrder = SortOrder.DESCENDING,
    val listMode: ListMode = ListMode.GRID,
    val showBalancedFolders: Boolean = false
)