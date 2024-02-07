package dev.ridill.rivo.settings.domain.appLock

import androidx.annotation.StringRes
import dev.ridill.rivo.R
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

enum class AppAutoLockInterval(
    val duration: Duration,
    @StringRes val labelRes: Int
) {
    ONE_MINUTE(1.minutes, R.string.one_minute),
    FIVE_MINUTE(5.minutes, R.string.five_minutes),
    TEN_MINUTE(10.minutes, R.string.ten_minutes),
    ONE_HOUR(1.hours, R.string.one_hour)
}