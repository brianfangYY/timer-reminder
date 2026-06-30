package com.timer.reminder.ui.reminder

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.timer.reminder.data.local.entity.ReminderEntity
import com.timer.reminder.data.local.entity.TaskEntity
import com.timer.reminder.data.repository.ReminderRepository
import com.timer.reminder.data.repository.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReminderViewModel @Inject constructor(
    private val reminderRepository: ReminderRepository,
    private val taskRepository: TaskRepository
) : ViewModel() {

    val reminders: StateFlow<List<ReminderEntity>> = reminderRepository
        .getAllReminders()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pendingTasks: StateFlow<List<TaskEntity>> = taskRepository
        .getPendingTasks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addReminder(title: String, description: String, triggerAtMillis: Long, linkedTaskId: Long? = null) {
        viewModelScope.launch {
            reminderRepository.insert(
                ReminderEntity(
                    title = title,
                    description = description,
                    triggerAtMillis = triggerAtMillis,
                    linkedTaskId = linkedTaskId
                )
            )
        }
    }

    fun deleteReminder(id: Long) {
        viewModelScope.launch {
            reminderRepository.deleteById(id)
        }
    }
}
