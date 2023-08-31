package dev.ridill.mym.core.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import dev.ridill.mym.expense.data.local.ExpenseDao
import dev.ridill.mym.expense.data.local.TagsDao
import dev.ridill.mym.expense.data.local.entity.ExpenseEntity
import dev.ridill.mym.expense.data.local.entity.TagEntity
import dev.ridill.mym.settings.data.local.ConfigKeys
import dev.ridill.mym.settings.data.local.ConfigDao
import dev.ridill.mym.settings.data.local.entity.ConfigEntity

@Database(
    entities = [
        ExpenseEntity::class,
        TagEntity::class,
        ConfigEntity::class
    ],
    version = 3,
    exportSchema = false
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

val Migration_2_3 = object : Migration(2, 3) {
    override fun migrate(database: SupportSQLiteDatabase) {
        migrateTagEntity(database)
        migrateExpenseEntity(database)
    }

    private fun migrateTagEntity(database: SupportSQLiteDatabase) {
        database.execSQL(
            """
           CREATE TABLE tag_temp AS SELECT * FROM TagEntity
        """.trimIndent()
        )
        database.execSQL("DROP TABLE TagEntity")
        database.execSQL(
            """
            CREATE TABLE TagEntity(
            id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
            name TEXT NOT NULL,
            colorCode INTEGER NOT NULL,
            createdTimestamp STRING NOT NULL
            )
        """.trimIndent()
        )

        database.execSQL(
            """
            INSERT INTO TagEntity(name, colorCode, createdTimestamp)
            SELECT name, colorCode, dateCreated from tag_temp
        """.trimIndent()
        )
        database.execSQL("DROP TABLE tag_temp")
    }

    private fun migrateExpenseEntity(database: SupportSQLiteDatabase) {
        database.execSQL(
            """
            CREATE TABLE exp_temp AS SELECT * FROM ExpenseEntity
        """.trimIndent()
        )
        database.execSQL("DROP TABLE ExpenseEntity")
        database.execSQL(
            """
            CREATE TABLE ExpenseEntity(
            id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
            note TEXT NOT NULL,
            amount INTEGER NOT NULL,
            timestamp TEXT NOT NULL,
            tagId INTEGER,
            FOREIGN KEY(tagId) REFERENCES TagEntity(id) ON DELETE SET NULL
            )
        """.trimIndent()
        )
        database.execSQL("CREATE INDEX index_ExpenseEntity_tagId ON ExpenseEntity(tagId)")
        database.execSQL(
            """
            INSERT INTO ExpenseEntity(id, note, amount, timestamp, tagId)
            SELECT exp.id, exp.note, exp.amount, exp.dateTime, (SELECT tag.id FROM TagEntity tag WHERE tag.name = exp.tagId) FROM exp_temp exp
        """.trimIndent()
        )
        database.execSQL("DROP TABLE exp_temp")
    }
}