package com.timer.reminder.ui.tomato

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.timer.reminder.data.local.entity.TomatoRecordEntity
import com.timer.reminder.data.repository.TomatoRepository
import com.timer.reminder.service.TomatoForegroundService
import com.timer.reminder.service.TomatoStateHolder
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject

enum class TomatoState {
    IDLE, WORKING, BREAK
}

data class TomatoUiState(
    val state: TomatoState = TomatoState.IDLE,
    val remainingSeconds: Int = 25 * 60,
    val workDuration: Int = 25,
    val breakDuration: Int = 5,
    val totalCompleted: Int = 0,
    val currentSessionTask: String = "",
    val isTickSoundEnabled: Boolean = false
)

@HiltViewModel
class TomatoViewModel @Inject constructor(
    private val application: Application,
    private val tomatoRepository: TomatoRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TomatoUiState())
    val uiState: StateFlow<TomatoUiState> = _uiState.asStateFlow()

    private val _tickEvent = MutableSharedFlow<Unit>(replay = 0)
    val tickEvent: SharedFlow<Unit> = _tickEvent.asSharedFlow()

    private var timerJob: Job? = null

    fun toggleTickSound() {
        _uiState.update { it.copy(isTickSoundEnabled = !it.isTickSoundEnabled) }
    }

    init {
        tomatoRepository.getCompletedCount().onEach { count ->
            _uiState.update { it.copy(totalCompleted = count) }
        }.launchIn(viewModelScope)

        // Sync time from foreground service when it's running
        viewModelScope.launch {
            TomatoStateHolder.remainingSeconds.collect { serviceSeconds ->
                if (serviceSeconds > 0 && TomatoStateHolder.isServiceRunning.value) {
                    _uiState.update { it.copy(remainingSeconds = serviceSeconds.toInt()) }
                }
            }
        }
    }

    fun startWork(taskDescription: String = "") {
        val workSeconds = _uiState.value.workDuration * 60
        _uiState.update {
            it.copy(
                state = TomatoState.WORKING,
                remainingSeconds = workSeconds,
                currentSessionTask = taskDescription
            )
        }
        // Start foreground service to show notification countdown
        TomatoForegroundService.startService(application, workSeconds.toLong(), "focus")
        startTimer()
    }

    fun startBreak() {
        val breakSeconds = _uiState.value.breakDuration * 60
        _uiState.update {
            it.copy(
                state = TomatoState.BREAK,
                remainingSeconds = breakSeconds
            )
        }
        // Start foreground service to show notification countdown
        TomatoForegroundService.startService(application, breakSeconds.toLong(), "break")
        startTimer()
    }

    fun stop() {
        timerJob?.cancel()
        timerJob = null
        _uiState.update { it.copy(state = TomatoState.IDLE) }
        TomatoForegroundService.stopService(application)
    }

    fun reset() {
        timerJob?.cancel()
        timerJob = null
        _uiState.update {
            it.copy(
                state = TomatoState.IDLE,
                remainingSeconds = it.workDuration * 60
            )
        }
        TomatoForegroundService.stopService(application)
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            val startTime = System.currentTimeMillis()
            while (isActive) {
                delay(1000L)
                val current = _uiState.value

                // Emit tick event if sound is enabled
                if (current.isTickSoundEnabled && current.state != TomatoState.IDLE) {
                    _tickEvent.emit(Unit)
                }

                val newRemaining = current.remainingSeconds - 1
                if (newRemaining <= 0) {
                    // Session complete
                    if (current.state == TomatoState.WORKING) {
                        // Save completed tomato
                        tomatoRepository.insert(
                            TomatoRecordEntity(
                                startTime = startTime,
                                endTime = System.currentTimeMillis(),
                                durationMinutes = current.workDuration,
                                completed = true,
                                taskDescription = current.currentSessionTask
                            )
                        )
                    }
                    _uiState.update {
                        it.copy(state = TomatoState.IDLE, remainingSeconds = 0)
                    }
                    // Stop foreground service
                    TomatoForegroundService.stopService(application)
                    break
                }
                _uiState.update { it.copy(remainingSeconds = newRemaining) }
            }
        }
    }

    fun updateWorkDuration(minutes: Int) {
        _uiState.update {
            it.copy(
                workDuration = minutes,
                remainingSeconds = minutes * 60
            )
        }
    }

    fun updateBreakDuration(minutes: Int) {
        _uiState.update { it.copy(breakDuration = minutes) }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
        TomatoForegroundService.stopService(application)
    }
}
