package com.timer.reminder.data.repository

import com.timer.reminder.data.local.dao.AlarmDao
import com.timer.reminder.data.local.entity.AlarmEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AlarmRepositoryImpl @Inject constructor(
    private val alarmDao: AlarmDao
) : AlarmRepository {

    override fun getAllAlarms(): Flow<List<AlarmEntity>> = alarmDao.getAllAlarms()

    override suspend fun getAlarmById(id: Long): AlarmEntity? = alarmDao.getAlarmById(id)

    override suspend fun insert(alarm: AlarmEntity): Long = alarmDao.insert(alarm)

    override suspend fun update(alarm: AlarmEntity) = alarmDao.update(alarm)

    override suspend fun deleteById(id: Long) = alarmDao.deleteById(id)
}
