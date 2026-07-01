package com.timer.reminder.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.timer.reminder.data.local.dao.*
import com.timer.reminder.data.local.entity.*

@Database(
    entities = [
        ReminderEntity::class,
        TomatoRecordEntity::class,
        TaskEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun reminderDao(): ReminderDao
    abstract fun tomatoRecordDao(): TomatoRecordDao
    abstract fun taskDao(): TaskDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "timer_reminder_db"
                )
                    .addMigrations(
                        MIGRATION_1_2,
                        MIGRATION_2_3,
                        MIGRATION_3_4
                    )
                    .build()
                    .also { INSTANCE = it }
            }
        }

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE alarms ADD COLUMN linkedTaskId INTEGER DEFAULT NULL")
                db.execSQL("ALTER TABLE reminders ADD COLUMN linkedTaskId INTEGER DEFAULT NULL")
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("DROP TABLE IF EXISTS alarms")
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Reminders table: add periodic repeat fields with defaults
                db.execSQL("""
                    ALTER TABLE reminders 
                    ADD COLUMN repeatType TEXT NOT NULL DEFAULT 'none'
                """)
                db.execSQL("""
                    ALTER TABLE reminders 
                    ADD COLUMN repeatDaysOfWeek TEXT NOT NULL DEFAULT '[]'
                """)
                db.execSQL("""
                    ALTER TABLE reminders 
                    ADD COLUMN repeatDaysOfMonth TEXT NOT NULL DEFAULT '[]'
                """)
            }
        }
    }
}
