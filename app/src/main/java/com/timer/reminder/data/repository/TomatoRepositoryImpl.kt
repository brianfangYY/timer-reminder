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

    override suspend fun insert(record: TomatoRecordEntity): Long = tomatoRecordDao.insert(record)

    override suspend fun deleteById(id: Long) = tomatoRecordDao.deleteById(id)
}
