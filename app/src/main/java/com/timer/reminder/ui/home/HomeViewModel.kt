package com.timer.reminder.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.timer.reminder.data.local.entity.ReminderEntity
import com.timer.reminder.data.local.entity.TaskEntity
import com.timer.reminder.data.local.entity.TomatoRecordEntity
import com.timer.reminder.data.repository.ReminderRepository
import com.timer.reminder.data.repository.TaskRepository
import com.timer.reminder.data.repository.TomatoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val todayTomatoCount: Int = 0,
    val pendingTaskCount: Int = 0,
    val upcomingReminders: List<ReminderEntity> = emptyList()
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val reminderRepository: ReminderRepository,
    private val tomatoRepository: TomatoRepository,
    private val taskRepository: TaskRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        collectTomatoCount()
        collectTaskCount()
        collectUpcomingReminders()
    }

    private fun collectTomatoCount() {
        tomatoRepository.getCompletedCount().onEach { count ->
            _uiState.update { it.copy(todayTomatoCount = count) }
        }.launchIn(viewModelScope)
    }

    private fun collectTaskCount() {
        taskRepository.getPendingTasks().onEach { tasks ->
            _uiState.update { it.copy(pendingTaskCount = tasks.size) }
        }.launchIn(viewModelScope)
    }

    private fun collectUpcomingReminders() {
        reminderRepository.getAllReminders().onEach { reminders ->
            _uiState.update { it.copy(upcomingReminders = reminders.take(5)) }
        }.launchIn(viewModelScope)
    }
}
