package com.timer.reminder.data.repository

import com.timer.reminder.data.local.dao.TaskDao
import com.timer.reminder.data.local.entity.TaskEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TaskRepositoryImpl @Inject constructor(
    private val taskDao: TaskDao
) : TaskRepository {

    override fun getAllTasks(): Flow<List<TaskEntity>> = taskDao.getAllTasks()

    override fun getPendingTasks(): Flow<List<TaskEntity>> = taskDao.getPendingTasks()

    override suspend fun getTaskById(id: Long): TaskEntity? = taskDao.getTaskById(id)

    override suspend fun insert(task: TaskEntity): Long = taskDao.insert(task)

    override suspend fun update(task: TaskEntity) = taskDao.update(task)

    override suspend fun deleteById(id: Long) = taskDao.deleteById(id)
}
