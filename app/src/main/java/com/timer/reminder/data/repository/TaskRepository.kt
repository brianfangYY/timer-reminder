package com.timer.reminder.data.repository

import com.timer.reminder.data.local.entity.TaskEntity
import kotlinx.coroutines.flow.Flow

interface TaskRepository {
    fun getAllTasks(): Flow<List<TaskEntity>>
    fun getPendingTasks(): Flow<List<TaskEntity>>
    suspend fun getTaskById(id: Long): TaskEntity?
    suspend fun insert(task: TaskEntity): Long
    suspend fun update(task: TaskEntity)
    suspend fun deleteById(id: Long)
}
