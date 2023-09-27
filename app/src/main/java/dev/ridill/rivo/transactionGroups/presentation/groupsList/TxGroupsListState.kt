package dev.ridill.rivo.transactionGroups.presentation.groupsList

import android.icu.util.Currency
import dev.ridill.rivo.core.domain.model.ListMode
import dev.ridill.rivo.core.domain.util.CurrencyUtil
import dev.ridill.rivo.transactionGroups.domain.model.TxGroupDetails

data class TxGroupsListState(
    val currency: Currency = CurrencyUtil.default,
    val listMode: ListMode = ListMode.GRID,
    val groupsList: List<TxGroupDetails> = emptyList()
)