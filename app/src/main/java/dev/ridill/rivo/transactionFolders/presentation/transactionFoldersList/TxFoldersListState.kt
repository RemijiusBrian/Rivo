package dev.ridill.rivo.transactionFolders.presentation.transactionFoldersList

import android.icu.util.Currency
import dev.ridill.rivo.core.domain.model.ListMode
import dev.ridill.rivo.core.domain.util.LocaleUtil

data class TxFoldersListState(
    val currency: Currency = LocaleUtil.defaultCurrency,
    val listMode: ListMode = ListMode.GRID,
)