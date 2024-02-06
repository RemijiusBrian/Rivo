package dev.ridill.rivo.settings.domain.appLock

import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

enum class AppAutoLockInterval(
    val duration: Duration
) {
    ONE_MINUTE(1.minutes),
    FIVE_MINUTE(5.minutes),
    TEN_MINUTE(10.minutes),
    ONE_HOUR(1.hours)
}