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

    /**
     * Add a new reminder with repeat schedule support.
     *
     * @param title reminder title
     * @param description optional description
     * @param triggerAtMillis initial/one-shot trigger time
     * @param repeatType "none" | "daily" | "workdays" | "weekly" | "monthly"
     * @param repeatDaysOfWeek e.g. "[2,4,6]" for Mon/Wed/Fri (Calendar.DAY_OF_WEEK values)
     * @param repeatDaysOfMonth e.g. "[1,15]" for 1st and 15th of each month
     * @param linkedTaskId optional task to link
     */
    fun addReminder(
        title: String,
        description: String = "",
        triggerAtMillis: Long,
        repeatType: String = "none",
        repeatDaysOfWeek: String = "[]",
        repeatDaysOfMonth: String = "[]",
        linkedTaskId: Long? = null
    ) {
        viewModelScope.launch {
            val isRepeating = repeatType != "none"
            // For repeating reminders, compute the first actual trigger
            val firstTrigger = if (isRepeating) {
                val tempReminder = ReminderEntity(
                    title = title,
                    description = description,
                    triggerAtMillis = triggerAtMillis,
                    repeatType = repeatType,
                    repeatDaysOfWeek = repeatDaysOfWeek,
                    repeatDaysOfMonth = repeatDaysOfMonth,
                    isRepeating = true,
                    linkedTaskId = linkedTaskId
                )
                ReminderScheduler.computeNextTrigger(tempReminder) ?: triggerAtMillis
            } else {
                triggerAtMillis
            }

            val id = reminderRepository.insert(
                ReminderEntity(
                    title = title,
                    description = description,
                    triggerAtMillis = firstTrigger,
                    isRepeating = isRepeating,
                    repeatType = repeatType,
                    repeatDaysOfWeek = repeatDaysOfWeek,
                    repeatDaysOfMonth = repeatDaysOfMonth,
                    linkedTaskId = linkedTaskId
                )
            )

            // Schedule the alarm (for repeating types, this schedules the first occurrence)
            ReminderScheduler.schedule(context, id, firstTrigger, title, description, linkedTaskId)
        }
    }

    fun deleteReminder(id: Long) {
        viewModelScope.launch {
            ReminderScheduler.cancel(context, id)
            reminderRepository.deleteById(id)
        }
    }
}
