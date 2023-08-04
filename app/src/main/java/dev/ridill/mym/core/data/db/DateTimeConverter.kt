package dev.ridill.mym.core.data.db

import androidx.room.TypeConverter
import dev.ridill.mym.core.domain.util.tryOrNull
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class DateTimeConverter {

    @TypeConverter
    fun fromTimestamp(value: String?): LocalDateTime? = tryOrNull {
        LocalDateTime.parse(value, DateTimeFormatter.ISO_DATE_TIME)
    }

    @TypeConverter
    fun dateToTimestamp(dateTime: LocalDateTime?): String? = tryOrNull {
        dateTime?.format(DateTimeFormatter.ISO_DATE_TIME)
    }
}