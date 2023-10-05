package dev.ridill.rivo.core.data.db

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import dev.ridill.rivo.BuildConfig
import dev.ridill.rivo.settings.data.local.ConfigDao
import dev.ridill.rivo.settings.data.local.ConfigKeys
import dev.ridill.rivo.settings.data.local.entity.ConfigEntity
import dev.ridill.rivo.folders.data.local.FolderDao
import dev.ridill.rivo.folders.data.local.entity.FolderEntity
import dev.ridill.rivo.transactions.data.local.TagsDao
import dev.ridill.rivo.transactions.data.local.TransactionDao
import dev.ridill.rivo.transactions.data.local.entity.TagEntity
import dev.ridill.rivo.transactions.data.local.entity.TransactionEntity

@Database(
    entities = [
        TransactionEntity::class,
        FolderEntity::class,
        TagEntity::class,
        ConfigEntity::class
    ],
    version = BuildConfig.DB_VERSION,
    autoMigrations = [
        AutoMigration(from = 5, to = 6)
    ]
)
@TypeConverters(DateTimeConverter::class)
abstract class RivoDatabase : RoomDatabase() {
    companion object {
        const val NAME = "MYM.db"
        const val DEFAULT_ID_LONG = 0L
    }

    // Dao Methods
    abstract fun transactionDao(): TransactionDao
    abstract fun folderDao(): FolderDao
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

val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
            """
            ALTER TABLE ExpenseEntity
            ADD COLUMN isExcludedFromExpenditure INTEGER NOT NULL DEFAULT 0
        """.trimIndent()
        )
    }
}