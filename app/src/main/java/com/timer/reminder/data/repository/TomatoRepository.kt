package com.timer.reminder.data.repository

import com.timer.reminder.data.local.entity.TomatoRecordEntity
import kotlinx.coroutines.flow.Flow

interface TomatoRepository {
    fun getAllRecords(): Flow<List<TomatoRecordEntity>>
    fun getCompletedCount(): Flow<Int>
    suspend fun insert(record: TomatoRecordEntity): Long
    suspend fun deleteById(id: Long)
}
