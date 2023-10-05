package dev.ridill.rivo.folders.presentation.folderDetails

import android.icu.util.Currency
import dev.ridill.rivo.core.domain.util.LocaleUtil
import dev.ridill.rivo.core.domain.util.DateUtil
import dev.ridill.rivo.core.domain.util.Zero
import dev.ridill.rivo.core.ui.navigation.destinations.ARG_INVALID_ID_LONG
import dev.ridill.rivo.transactions.domain.model.TransactionType
import java.time.LocalDateTime

data class FolderDetailsState(
    val folderId: Long = ARG_INVALID_ID_LONG,
    val isNewFolder: Boolean = true,
    val editModeActive: Boolean = false,
    val showDeleteConfirmation: Boolean = false,
    val createdTimestamp: LocalDateTime = DateUtil.now(),
    val isExcluded: Boolean = false,
    val currency: Currency = LocaleUtil.defaultCurrency,
    val aggregateAmount: Double = Double.Zero,
    val aggregateType: TransactionType? = null
) {
    val createdTimestampFormatted: String
        get() = createdTimestamp.format(DateUtil.Formatters.localizedDateMedium)
}