package dev.ridill.rivo.core.domain.util

import android.app.PendingIntent

object UtilConstants {
    const val DB_MONTH_AND_YEAR_FORMAT = "%m-%Y"
    const val DEBOUNCE_TIMEOUT = 250L
    const val DEFAULT_PAGE_SIZE = 10

    val pendingIntentFlags: Int
        get() = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
}