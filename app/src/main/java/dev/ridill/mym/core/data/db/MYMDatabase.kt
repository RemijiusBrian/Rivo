package dev.ridill.mym.core.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import dev.ridill.mym.expense.data.local.ExpenseDao
import dev.ridill.mym.expense.data.local.entity.ExpenseEntity
import dev.ridill.mym.expense.data.local.entity.TagEntity

@Database(
    entities = [
        ExpenseEntity::class,
        TagEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(DateTimeConverter::class)
abstract class MYMDatabase : RoomDatabase() {
    companion object {
        const val NAME = "MYM.db"
    }

    // Dao Methods
    abstract fun expenseDao(): ExpenseDao
}