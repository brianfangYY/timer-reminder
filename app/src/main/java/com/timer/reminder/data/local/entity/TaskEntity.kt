package com.timer.reminder.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String = "",
    val isCompleted: Boolean = false,
    val priority: Int = 0, // 0=low, 1=medium, 2=high
    val dueDate: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
)
