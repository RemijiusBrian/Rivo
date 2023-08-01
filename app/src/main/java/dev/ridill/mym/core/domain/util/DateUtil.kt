package dev.ridill.mym.core.domain.util

import androidx.annotation.StringRes
import dev.ridill.mym.R
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.time.format.FormatStyle
import java.time.temporal.ChronoField

object DateUtil {
    fun now(): LocalDateTime = LocalDateTime.now()

    fun getPartOfDay(): PartOfDay = when (now().hour) {
        in (0..11) -> PartOfDay.MORNING
        12 -> PartOfDay.NOON
        in (13..15) -> PartOfDay.AFTERNOON
        else -> PartOfDay.EVENING
    }

    fun currentMonthYear(): String = now()
        .format(Formatters.MM_yyyy_hyphenSep)

    object Formatters {
        val MM_yyyy_hyphenSep: DateTimeFormatter
            get() = DateTimeFormatter.ofPattern("MM-yyyy")

        val localizedTimeShort: DateTimeFormatter
            get() = DateTimeFormatter
                .ofLocalizedTime(FormatStyle.SHORT)
        val ddth_MMM: DateTimeFormatter
            get() = DateTimeFormatterBuilder()
                .appendText(ChronoField.DAY_OF_MONTH, ordinalsMap)
                .appendPattern(" MMM")
                .toFormatter()

        private val ordinalsMap: Map<Long, String>
            get() {
                val mutableMap = mutableMapOf(
                    1L to "1st",
                    2L to "2nd",
                    3L to "3rd",
                    21L to "21st",
                    22L to "22nd",
                    23L to "23rd",
                    31L to "31st",
                ).also { map ->
                    (1L..31L).forEach { map.putIfAbsent(it, "th") }
                }

                return mutableMap.toMap()
            }
    }
}

enum class PartOfDay(
    @StringRes val labelRes: Int
) {
    MORNING(R.string.part_of_day_morning),
    NOON(R.string.part_of_day_noon),
    AFTERNOON(R.string.part_of_day_afternoon),
    EVENING(R.string.part_of_day_evening)
}