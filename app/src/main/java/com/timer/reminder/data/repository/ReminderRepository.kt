package com.timer.reminder.data.repository

import com.timer.reminder.data.local.entity.ReminderEntity
import kotlinx.coroutines.flow.Flow

interface ReminderRepository {
    fun getAllReminders(): Flow<List<ReminderEntity>>
    suspend fun getReminderById(id: Long): ReminderEntity?
    suspend fun insert(reminder: ReminderEntity): Long
    suspend fun update(reminder: ReminderEntity)
    suspend fun deleteById(id: Long)
}
