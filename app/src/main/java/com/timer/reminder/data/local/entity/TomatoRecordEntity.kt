package com.timer.reminder.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tomato_records")
data class TomatoRecordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val startTime: Long,
    val endTime: Long,
    val durationMinutes: Int = 25,
    val completed: Boolean = true,
    val taskDescription: String = ""
)
