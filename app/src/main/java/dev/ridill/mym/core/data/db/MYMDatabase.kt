package dev.ridill.mym.core.data.db

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import dev.ridill.mym.BuildConfig
import dev.ridill.mym.expense.data.local.ExpenseDao
import dev.ridill.mym.expense.data.local.TagsDao
import dev.ridill.mym.expense.data.local.entity.ExpenseEntity
import dev.ridill.mym.expense.data.local.entity.TagEntity
import dev.ridill.mym.settings.data.local.ConfigDao
import dev.ridill.mym.settings.data.local.ConfigKeys
import dev.ridill.mym.settings.data.local.entity.ConfigEntity

@Database(
    entities = [
        ExpenseEntity::class,
        TagEntity::class,
        ConfigEntity::class
    ],
    version = BuildConfig.DB_VERSION,
    autoMigrations = [
        AutoMigration(from = 3, to = 4)
    ]
)
@TypeConverters(DateTimeConverter::class)
abstract class MYMDatabase : RoomDatabase() {
    companion object {
        const val NAME = "MYM.db"
        const val DEFAULT_ID_LONG = 0L
    }

    // Dao Methods
    abstract fun expenseDao(): ExpenseDao
    abstract fun tagsDao(): TagsDao
    abstract fun configDao(): ConfigDao
}

val Migration_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
            """
            CREATE TABLE MiscConfigEntity(
            configKey TEXT PRIMARY KEY NOT NULL,
            configValue TEXT NOT NULL
            )
        """.trimIndent()
        )
        val cursor = database
            .query("SELECT IFNULL(amount, 0) as amount FROM BudgetEntity WHERE isCurrent = 1")
        cursor.moveToFirst()
        val currentBudget = cursor.getLong(0)

        database.execSQL("INSERT INTO MiscConfigEntity VALUES('${ConfigKeys.BUDGET_AMOUNT}', '$currentBudget')")
        database.execSQL("DROP TABLE BudgetEntity")
    }
}