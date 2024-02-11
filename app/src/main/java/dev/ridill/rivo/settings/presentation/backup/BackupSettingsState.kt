package dev.ridill.rivo.settings.presentation.backup

import dev.ridill.rivo.core.domain.util.DateUtil
import dev.ridill.rivo.core.ui.util.UiText
import dev.ridill.rivo.settings.domain.modal.BackupInterval
import java.time.LocalDateTime
import java.time.chrono.IsoChronology
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.time.format.FormatStyle
import java.util.Locale

data class BackupSettingsState(
    val accountEmail: String? = null,
    val isAccountAdded: Boolean = false,
    val interval: BackupInterval = BackupInterval.MANUAL,
    val showBackupIntervalSelection: Boolean = false,
    val isBackupRunning: Boolean = false,
    val lastBackupDateTime: LocalDateTime? = null
) {
    val lastBackupDateFormatted: UiText?
        get() = lastBackupDateTime?.let { DateUtil.Formatters.prettyDateAgo(it.toLocalDate()) }

    fun getBackupTimeFormatted(is24HourFormat: Boolean): String? {
        var pattern = DateTimeFormatterBuilder.getLocalizedDateTimePattern(
            null,
            FormatStyle.SHORT,
            IsoChronology.INSTANCE,
            Locale.getDefault()
        )

        if (is24HourFormat) {
            pattern = pattern.replace("h", "H")
                .replace("a", "").trim()
        }

        val formatter = DateTimeFormatter.ofPattern(pattern)

        return lastBackupDateTime?.let { formatter.format(it) }
    }
}