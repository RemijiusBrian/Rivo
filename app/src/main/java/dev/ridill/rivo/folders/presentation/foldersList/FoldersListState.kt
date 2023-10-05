package dev.ridill.rivo.folders.presentation.foldersList

import android.icu.util.Currency
import dev.ridill.rivo.core.domain.model.ListMode
import dev.ridill.rivo.core.domain.model.SortCriteria
import dev.ridill.rivo.core.domain.model.SortOrder
import dev.ridill.rivo.core.domain.util.LocaleUtil

data class FoldersListState(
    val currency: Currency = LocaleUtil.defaultCurrency,
    val sortCriteria: SortCriteria = SortCriteria.BY_NAME,
    val sortOrder: SortOrder = SortOrder.ASCENDING,
    val listMode: ListMode = ListMode.GRID
)