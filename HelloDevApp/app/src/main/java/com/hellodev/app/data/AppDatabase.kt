package com.hellodev.app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        RubberStock::class, 
        Sale::class, 
        StockCategory::class, 
        StockDraft::class,
        ScheduledEvent::class,
        Payment::class,
        PaymentTransaction::class,
        Note::class
    ], 
    version = 6, 
    exportSchema = false
)
@androidx.room.TypeConverters(PaymentTypeConverter::class, DraftItemsConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun stockDao(): StockDao
    abstract fun saleDao(): SaleDao
    abstract fun categoryDao(): StockCategoryDao
    abstract fun draftDao(): StockDraftDao
    abstract fun eventDao(): ScheduledEventDao
    abstract fun paymentDao(): PaymentDao
    abstract fun paymentTransactionDao(): PaymentTransactionDao
    abstract fun noteDao(): NoteDao
    
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        // Migration from version 5 to 6 - Adds Calendar, Payments, and Notes tables
        private val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Create scheduled_events table for calendar functionality
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `scheduled_events` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `title` TEXT NOT NULL,
                        `description` TEXT NOT NULL,
                        `eventDate` INTEGER NOT NULL,
                        `notificationTime` INTEGER NOT NULL,
                        `isCompleted` INTEGER NOT NULL,
                        `createdAt` INTEGER NOT NULL,
                        `notes` TEXT NOT NULL
                    )
                """.trimIndent())
                
                // Create payments table for payment tracking
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `payments` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `type` TEXT NOT NULL,
                        `partyName` TEXT NOT NULL,
                        `totalAmount` REAL NOT NULL,
                        `paidAmount` REAL NOT NULL,
                        `dueDate` INTEGER NOT NULL,
                        `remark` TEXT NOT NULL,
                        `notes` TEXT NOT NULL,
                        `createdAt` INTEGER NOT NULL,
                        `isFullyPaid` INTEGER NOT NULL
                    )
                """.trimIndent())
                
                // Create payment_transactions table for partial payment history
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `payment_transactions` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `paymentId` INTEGER NOT NULL,
                        `amount` REAL NOT NULL,
                        `transactionDate` INTEGER NOT NULL,
                        `remark` TEXT NOT NULL,
                        FOREIGN KEY(`paymentId`) REFERENCES `payments`(`id`) ON DELETE CASCADE
                    )
                """.trimIndent())
                
                // Create notes table for user notes
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `notes` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `title` TEXT NOT NULL,
                        `content` TEXT NOT NULL,
                        `createdAt` INTEGER NOT NULL,
                        `updatedAt` INTEGER NOT NULL,
                        `color` INTEGER NOT NULL
                    )
                """.trimIndent())
            }
        }
        
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "rubber_inventory_database"
                )
                .addMigrations(MIGRATION_5_6)
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
