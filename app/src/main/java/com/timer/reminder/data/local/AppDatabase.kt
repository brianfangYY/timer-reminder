package com.timer.reminder.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.timer.reminder.data.local.dao.*
import com.timer.reminder.data.local.entity.*

@Database(
    entities = [
        ReminderEntity::class,
        TomatoRecordEntity::class,
        AlarmEntity::class,
        TaskEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun reminderDao(): ReminderDao
    abstract fun tomatoRecordDao(): TomatoRecordDao
    abstract fun alarmDao(): AlarmDao
    abstract fun taskDao(): TaskDao
}
