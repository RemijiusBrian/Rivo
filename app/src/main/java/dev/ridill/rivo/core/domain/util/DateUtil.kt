package dev.ridill.rivo.core.domain.util

import androidx.annotation.StringRes
import dev.ridill.rivo.R
import dev.ridill.rivo.core.ui.util.UiText
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.time.format.FormatStyle
import java.time.temporal.ChronoField
import java.time.temporal.ChronoUnit

object DateUtil {
    fun now(): LocalDateTime = LocalDateTime.now()

    fun parse(value: String): LocalDateTime? = tryOrNull {
        LocalDateTime.parse(value)
    }

    fun getPartOfDay(): PartOfDay = when (now().hour) {
        in (0..11) -> PartOfDay.MORNING
        12 -> PartOfDay.NOON
        in (13..15) -> PartOfDay.AFTERNOON
        else -> PartOfDay.EVENING
    }

    fun fromMillis(
        millis: Long,
        zoneId: ZoneId = ZoneId.systemDefault()
    ): LocalDateTime = Instant.ofEpochMilli(millis)
        .atZone(zoneId)
        .toLocalDateTime()

    fun toMillis(
        dateTime: LocalDateTime,
        zoneId: ZoneId = ZoneId.systemDefault()
    ): Long = dateTime
        .atZone(zoneId)
        .toInstant()
        .toEpochMilli()

    fun dateFromMillisWithTime(
        millis: Long,
        time: LocalDateTime,
        zoneId: ZoneId = ZoneId.systemDefault()
    ): LocalDateTime {
        val date = Instant.ofEpochMilli(millis)
            .atZone(zoneId)
            .toLocalDate()

        return time
            .withDayOfMonth(date.dayOfMonth)
            .withMonth(date.monthValue)
            .withYear(date.year)
    }

    object Formatters {
        val MM_yyyy_dbFormat: DateTimeFormatter
            get() = DateTimeFormatter.ofPattern("MM-yyyy")

        val localizedTimeShort: DateTimeFormatter
            get() = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)

        val localizedDateMedium: DateTimeFormatter
            get() = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)

        val localizedDateLong: DateTimeFormatter
            get() = DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG)

        val ddth_EEE_spaceSep: DateTimeFormatter
            get() = DateTimeFormatterBuilder()
                .appendText(ChronoField.DAY_OF_MONTH, ordinalsMap)
                .appendPattern(" EEE")
                .toFormatter()

        fun prettyDateAgo(
            date: LocalDate
        ): UiText {
            val currentDate = now()
            if (date.isAfter(currentDate.toLocalDate())) return UiText.DynamicString(String.Empty)

            val daysDiff = ChronoUnit.DAYS.between(date, currentDate)
                .coerceAtLeast(Long.Zero)
                .toInt()
            if (daysDiff < 1)
                return UiText.StringResource(R.string.today)

            val weeksDiff = ChronoUnit.WEEKS.between(date, currentDate)
                .toInt()
            if (weeksDiff < 1L)
                return UiText.PluralResource(R.plurals.days_past, daysDiff, daysDiff.toString())

            val monthsDiff = ChronoUnit.MONTHS.between(date, currentDate)
            if (monthsDiff < 1L)
                return UiText.PluralResource(R.plurals.weeks_past, weeksDiff, weeksDiff.toString())

            return UiText.DynamicString(date.format(localizedDateLong))
        }

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
                    (1L..31L).forEach { map.putIfAbsent(it, "${it}th") }
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