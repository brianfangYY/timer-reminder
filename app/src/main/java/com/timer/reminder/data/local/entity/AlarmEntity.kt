package com.timer.reminder.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "alarms")
data class AlarmEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val label: String = "",
    val hourOfDay: Int,
    val minute: Int,
    val isEnabled: Boolean = true,
    val repeatDays: String = "", // "1,2,3,4,5,6,7" for Mon-Sun
    val useVibration: Boolean = true,
    val linkedTaskId: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
)
