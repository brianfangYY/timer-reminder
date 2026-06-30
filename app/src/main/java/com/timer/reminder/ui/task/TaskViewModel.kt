package com.timer.reminder.ui.task

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.timer.reminder.data.local.entity.TaskEntity
import com.timer.reminder.data.repository.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TaskViewModel @Inject constructor(
    private val taskRepository: TaskRepository
) : ViewModel() {

    val tasks: StateFlow<List<TaskEntity>> = taskRepository
        .getAllTasks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addTask(title: String, description: String, priority: Int = 0, dueDate: Long? = null) {
        viewModelScope.launch {
            taskRepository.insert(TaskEntity(
                title = title,
                description = description,
                priority = priority,
                dueDate = dueDate
            ))
        }
    }

    fun toggleTask(task: TaskEntity) {
        viewModelScope.launch {
            taskRepository.update(task.copy(isCompleted = !task.isCompleted))
        }
    }

    fun deleteTask(id: Long) {
        viewModelScope.launch {
            taskRepository.deleteById(id)
        }
    }
}
