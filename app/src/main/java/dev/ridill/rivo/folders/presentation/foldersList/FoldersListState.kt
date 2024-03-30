package dev.ridill.rivo.folders.presentation.foldersList

import dev.ridill.rivo.core.domain.model.ListMode
import dev.ridill.rivo.core.domain.util.LocaleUtil
import java.util.Currency

data class FoldersListState(
    val currency: Currency = LocaleUtil.defaultCurrency,
    val listMode: ListMode = ListMode.GRID
)