package com.timer.reminder.data.local.dao

import androidx.room.*
import com.timer.reminder.data.local.entity.TomatoRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TomatoRecordDao {
    @Query("SELECT * FROM tomato_records ORDER BY startTime DESC")
    fun getAllRecords(): Flow<List<TomatoRecordEntity>>

    @Query("SELECT COUNT(*) FROM tomato_records WHERE completed = 1")
    fun getCompletedCount(): Flow<Int>

    @Query("SELECT * FROM tomato_records WHERE id = :id")
    suspend fun getRecordById(id: Long): TomatoRecordEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: TomatoRecordEntity): Long

    @Delete
    suspend fun delete(record: TomatoRecordEntity)

    @Query("DELETE FROM tomato_records WHERE id = :id")
    suspend fun deleteById(id: Long)
}
