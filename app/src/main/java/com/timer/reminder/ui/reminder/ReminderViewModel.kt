package com.timer.reminder.ui.reminder

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.timer.reminder.data.local.entity.ReminderEntity
import com.timer.reminder.data.local.entity.TaskEntity
import com.timer.reminder.data.repository.ReminderRepository
import com.timer.reminder.data.repository.TaskRepository
import com.timer.reminder.service.ReminderScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReminderViewModel @Inject constructor(
    private val reminderRepository: ReminderRepository,
    private val taskRepository: TaskRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    val reminders: StateFlow<List<ReminderEntity>> = reminderRepository
        .getAllReminders()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pendingTasks: StateFlow<List<TaskEntity>> = taskRepository
        .getPendingTasks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addReminder(title: String, description: String, triggerAtMillis: Long, linkedTaskId: Long? = null) {
        viewModelScope.launch {
            val id = reminderRepository.insert(
                ReminderEntity(
                    title = title,
                    description = description,
                    triggerAtMillis = triggerAtMillis,
                    linkedTaskId = linkedTaskId
                )
            )
            // Schedule the reminder alarm
            ReminderScheduler.schedule(context, id, triggerAtMillis, title, description)
        }
    }

    fun deleteReminder(id: Long) {
        viewModelScope.launch {
            ReminderScheduler.cancel(context, id)
            reminderRepository.deleteById(id)
        }
    }
}
