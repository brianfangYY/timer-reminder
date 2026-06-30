package com.timer.reminder.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reminders")
data class ReminderEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String = "",
    val triggerAtMillis: Long,
    val isRepeating: Boolean = false,
    val repeatIntervalMinutes: Long = 0,
    val isEnabled: Boolean = true,
    val linkedTaskId: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
)
