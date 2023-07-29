package dev.ridill.mym.core.domain.util

import androidx.annotation.StringRes
import dev.ridill.mym.R
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

object DateUtil {
    fun now(): LocalDateTime = LocalDateTime.now()

    fun getPartOfDay(): PartOfDay = when (now().hour) {
        in (0..11) -> PartOfDay.MORNING
        12 -> PartOfDay.NOON
        in (13..15) -> PartOfDay.AFTERNOON
        else -> PartOfDay.EVENING
    }

    fun currentMonthYear(): String = now()
        .format(Formatters.dbMonthAndYear)

    object Formatters {
        val dbMonthAndYear: DateTimeFormatter = DateTimeFormatter.ofPattern("MM-yyyy")
        val localizedLong: DateTimeFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG)
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