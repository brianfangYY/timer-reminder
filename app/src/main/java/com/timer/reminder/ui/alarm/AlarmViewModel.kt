package com.timer.reminder.ui.alarm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.timer.reminder.data.local.entity.AlarmEntity
import com.timer.reminder.data.repository.AlarmRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AlarmViewModel @Inject constructor(
    private val alarmRepository: AlarmRepository
) : ViewModel() {

    val alarms: StateFlow<List<AlarmEntity>> = alarmRepository
        .getAllAlarms()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addAlarm(hourOfDay: Int, minute: Int, label: String, repeatDays: String = "") {
        viewModelScope.launch {
            alarmRepository.insert(
                AlarmEntity(
                    label = label,
                    hourOfDay = hourOfDay,
                    minute = minute,
                    repeatDays = repeatDays
                )
            )
        }
    }

    fun toggleAlarm(alarm: AlarmEntity) {
        viewModelScope.launch {
            alarmRepository.update(alarm.copy(isEnabled = !alarm.isEnabled))
        }
    }

    fun deleteAlarm(id: Long) {
        viewModelScope.launch {
            alarmRepository.deleteById(id)
        }
    }
}
