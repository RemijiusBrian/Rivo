package dev.ridill.rivo.folders.domain.model

import androidx.annotation.StringRes
import dev.ridill.rivo.R

enum class FolderSortCriteria(
    @StringRes val labelRes: Int
) {
    BY_NAME(R.string.folder_sort_criteria_by_name),
    BY_CREATED(R.string.folder_sort_criteria_by_created),
    BY_AGGREGATE(R.string.folder_sort_criteria_by_aggregate)
}
