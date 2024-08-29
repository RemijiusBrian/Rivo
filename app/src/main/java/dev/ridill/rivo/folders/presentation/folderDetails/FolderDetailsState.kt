package dev.ridill.rivo.folders.presentation.folderDetails

import dev.ridill.rivo.core.domain.util.DateUtil
import dev.ridill.rivo.core.domain.util.Empty
import dev.ridill.rivo.core.domain.util.Zero
import dev.ridill.rivo.folders.domain.model.AggregateType
import java.time.LocalDateTime

data class FolderDetailsState(
    val folderNname: String = String.Empty,
    val createdTimestamp: LocalDateTime = DateUtil.now(),
    val isExcluded: Boolean = false,
    val aggregateAmount: Double = Double.Zero,
    val aggregateType: AggregateType = AggregateType.BALANCED,
    val showDeleteConfirmation: Boolean = false
) {
    val createdTimestampFormatted: String
        get() = createdTimestamp.format(DateUtil.Formatters.localizedDateMedium)
}