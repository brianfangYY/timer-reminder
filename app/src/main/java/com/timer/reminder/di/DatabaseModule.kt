package com.timer.reminder.di

import android.content.Context
import androidx.room.Room
import com.timer.reminder.data.local.AppDatabase
import com.timer.reminder.data.local.dao.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "timer_reminder_db"
        ).build()
    }

    @Provides
    fun provideReminderDao(database: AppDatabase): ReminderDao = database.reminderDao()

    @Provides
    fun provideTomatoRecordDao(database: AppDatabase): TomatoRecordDao = database.tomatoRecordDao()

    @Provides
    fun provideAlarmDao(database: AppDatabase): AlarmDao = database.alarmDao()

    @Provides
    fun provideTaskDao(database: AppDatabase): TaskDao = database.taskDao()
}
