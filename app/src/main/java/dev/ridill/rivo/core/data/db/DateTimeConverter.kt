package dev.ridill.rivo.core.data.db

import androidx.room.TypeConverter
import dev.ridill.rivo.core.domain.util.tryOrNull
import java.time.LocalDateTime

class DateTimeConverter {

    @TypeConverter
    fun fromDateTimeString(value: String?): LocalDateTime? = tryOrNull {
        value?.let { LocalDateTime.parse(it) }
    }

    @TypeConverter
    fun toDateTimeString(dateTime: LocalDateTime?): String? = tryOrNull {
        dateTime?.toString()
    }
}