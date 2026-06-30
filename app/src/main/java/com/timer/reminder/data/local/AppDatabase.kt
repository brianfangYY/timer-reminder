package com.timer.reminder.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.timer.reminder.data.local.dao.*
import com.timer.reminder.data.local.entity.*

@Database(
    entities = [
        ReminderEntity::class,
        TomatoRecordEntity::class,
        AlarmEntity::class,
        TaskEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun reminderDao(): ReminderDao
    abstract fun tomatoRecordDao(): TomatoRecordDao
    abstract fun alarmDao(): AlarmDao
    abstract fun taskDao(): TaskDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE alarms ADD COLUMN linkedTaskId INTEGER DEFAULT NULL")
                db.execSQL("ALTER TABLE reminders ADD COLUMN linkedTaskId INTEGER DEFAULT NULL")
            }
        }
    }
}
