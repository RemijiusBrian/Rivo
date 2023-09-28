package dev.ridill.rivo.transactionFolders.presentation.transactionFoldersList

import android.icu.util.Currency
import dev.ridill.rivo.core.domain.model.ListMode
import dev.ridill.rivo.core.domain.util.CurrencyUtil
import dev.ridill.rivo.transactionFolders.domain.model.TransactionFolderDetails

data class TxFoldersListState(
    val currency: Currency = CurrencyUtil.default,
    val listMode: ListMode = ListMode.GRID,
    val foldersList: List<TransactionFolderDetails> = emptyList()
)