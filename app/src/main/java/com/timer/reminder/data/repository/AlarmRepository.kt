package com.timer.reminder.data.repository

import com.timer.reminder.data.local.entity.AlarmEntity
import kotlinx.coroutines.flow.Flow

interface AlarmRepository {
    fun getAllAlarms(): Flow<List<AlarmEntity>>
    suspend fun getAlarmById(id: Long): AlarmEntity?
    suspend fun insert(alarm: AlarmEntity): Long
    suspend fun update(alarm: AlarmEntity)
    suspend fun deleteById(id: Long)
}
