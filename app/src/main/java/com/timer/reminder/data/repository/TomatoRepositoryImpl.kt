package com.timer.reminder.data.repository

import com.timer.reminder.data.local.dao.TomatoRecordDao
import com.timer.reminder.data.local.entity.TomatoRecordEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TomatoRepositoryImpl @Inject constructor(
    private val tomatoRecordDao: TomatoRecordDao
) : TomatoRepository {

    override fun getAllRecords(): Flow<List<TomatoRecordEntity>> = tomatoRecordDao.getAllRecords()

    override fun getCompletedCount(): Flow<Int> = tomatoRecordDao.getCompletedCount()

    override fun getTodayCompletedCount(): Flow<Int> {
        val cal = java.util.Calendar.getInstance()
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
        cal.set(java.util.Calendar.MINUTE, 0)
        cal.set(java.util.Calendar.SECOND, 0)
        cal.set(java.util.Calendar.MILLISECOND, 0)
        val dayStart = cal.timeInMillis
        val dayEnd = dayStart + 24 * 60 * 60 * 1000L
        return tomatoRecordDao.getTodayCompletedCount(dayStart, dayEnd)
    }

    override suspend fun insert(record: TomatoRecordEntity): Long = tomatoRecordDao.insert(record)

    override suspend fun deleteById(id: Long) = tomatoRecordDao.deleteById(id)
}
