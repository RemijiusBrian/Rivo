package dev.ridill.rivo.core.data.db

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RenameColumn
import androidx.room.RenameTable
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.AutoMigrationSpec
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import dev.ridill.rivo.folders.data.local.FolderDao
import dev.ridill.rivo.folders.data.local.entity.FolderEntity
import dev.ridill.rivo.folders.data.local.views.FolderAndAggregateAmountView
import dev.ridill.rivo.schedules.data.local.SchedulesDao
import dev.ridill.rivo.schedules.data.local.entity.ScheduleEntity
import dev.ridill.rivo.settings.data.local.BudgetDao
import dev.ridill.rivo.settings.data.local.ConfigDao
import dev.ridill.rivo.settings.data.local.CurrencyDao
import dev.ridill.rivo.settings.data.local.entity.BudgetEntity
import dev.ridill.rivo.settings.data.local.entity.ConfigEntity
import dev.ridill.rivo.settings.data.local.entity.CurrencyEntity
import dev.ridill.rivo.transactions.data.local.TagsDao
import dev.ridill.rivo.transactions.data.local.TransactionDao
import dev.ridill.rivo.transactions.data.local.entity.TagEntity
import dev.ridill.rivo.transactions.data.local.entity.TransactionEntity
import dev.ridill.rivo.transactions.data.local.views.TransactionDetailsView

@Database(
    entities = [
        BudgetEntity::class,
        TransactionEntity::class,
        TagEntity::class,
        FolderEntity::class,
        ScheduleEntity::class,
        CurrencyEntity::class,
        ConfigEntity::class
    ],
    views = [
        TransactionDetailsView::class,
        FolderAndAggregateAmountView::class
    ],
    version = 12,
    autoMigrations = [
        AutoMigration(from = 5, to = 6),
        AutoMigration(from = 6, to = 7, spec = RivoDatabase.AutoMigrationSpec6To7::class),
        AutoMigration(from = 7, to = 8, spec = RivoDatabase.AutoMigrationSpec7To8::class),
        AutoMigration(from = 8, to = 9),
        AutoMigration(from = 9, to = 10),
        AutoMigration(from = 10, to = 11)
    ]
)
@TypeConverters(DateTimeConverter::class)
abstract class RivoDatabase : RoomDatabase() {
    companion object {
        const val NAME = "MYM.db" // FIXME: Change db name
        const val DEFAULT_ID_LONG = 0L
    }

    // Dao Methods
    abstract fun budgetDao(): BudgetDao
    abstract fun transactionDao(): TransactionDao
    abstract fun tagsDao(): TagsDao
    abstract fun folderDao(): FolderDao
    abstract fun schedulesDao(): SchedulesDao
    abstract fun currencyDao(): CurrencyDao
    abstract fun configDao(): ConfigDao

    @RenameTable(fromTableName = "transaction_folder_table", toTableName = "folder_table")
    class AutoMigrationSpec6To7 : AutoMigrationSpec

    @RenameColumn(
        tableName = "transaction_table",
        fromColumnName = "transaction_type_name",
        toColumnName = "type"
    )
    class AutoMigrationSpec7To8 : AutoMigrationSpec
}

val MIGRATION_11_12 = object : Migration(startVersion = 11, endVersion = 12) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE schedules_table ADD COLUMN last_paid_date TEXT NULL DEFAULT NULL")
    }
}