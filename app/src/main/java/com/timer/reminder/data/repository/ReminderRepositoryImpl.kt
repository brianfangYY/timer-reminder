package com.timer.reminder.data.repository

import com.timer.reminder.data.local.dao.ReminderDao
import com.timer.reminder.data.local.entity.ReminderEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReminderRepositoryImpl @Inject constructor(
    private val reminderDao: ReminderDao
) : ReminderRepository {

    override fun getAllReminders(): Flow<List<ReminderEntity>> = reminderDao.getAllReminders()

    override suspend fun getReminderById(id: Long): ReminderEntity? = reminderDao.getReminderById(id)

    override suspend fun insert(reminder: ReminderEntity): Long = reminderDao.insert(reminder)

    override suspend fun update(reminder: ReminderEntity) = reminderDao.update(reminder)

    override suspend fun deleteById(id: Long) = reminderDao.deleteById(id)
}
