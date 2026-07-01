package com.timer.reminder.ui.tomato

import android.app.Application
import android.os.SystemClock
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.timer.reminder.data.local.entity.TomatoRecordEntity
import com.timer.reminder.data.repository.TomatoRepository
import com.timer.reminder.service.TomatoForegroundService
import com.timer.reminder.service.TomatoStateHolder
import com.timer.reminder.util.TimerPersistence
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
    // Track the absolute end time using elapsedRealtime (immune to process freeze)
    private var timerEndElapsed: Long = 0L

    fun toggleTickSound() {
        _uiState.update { it.copy(isTickSoundEnabled = !it.isTickSoundEnabled) }
    }

    init {
        tomatoRepository.getCompletedCount().onEach { count ->
            _uiState.update { it.copy(totalCompleted = count) }
        }.launchIn(viewModelScope)

        // Attempt to restore saved timer state (survived process death)
        restoreSavedTimer()

        // Sync time from foreground service when it's running
        viewModelScope.launch {
            TomatoStateHolder.remainingSeconds.collect { serviceSeconds ->
                if (serviceSeconds > 0 && TomatoStateHolder.isServiceRunning.value) {
                    _uiState.update { it.copy(remainingSeconds = serviceSeconds.toInt()) }
                }
            }
        }
    }

    /** Restore timer if it was saved before process death */
    private fun restoreSavedTimer() {
        val saved = TimerPersistence.restoreTimerState(application)
        if (saved == null) return

        val remainingMs = saved.endElapsed - SystemClock.elapsedRealtime()
        if (remainingMs <= 0) {
            // Timer already expired while we were gone — clean up
            TimerPersistence.clearTimerState(application)
            return
        }

        val remainingSec = (remainingMs / 1000L).toInt()
        timerEndElapsed = saved.endElapsed

        _uiState.update {
            it.copy(
                state = TomatoState.valueOf(saved.stateName),
                remainingSeconds = remainingSec,
                workDuration = saved.workDuration,
                breakDuration = saved.breakDuration,
                currentSessionTask = saved.taskDescription
            )
        }

        // Restart foreground service to show notification
        val phase = if (saved.stateName == "WORKING") "focus" else "break"
        TomatoForegroundService.startService(application, remainingSec.toLong(), phase)

        // Resume the tick loop
        startTimer()
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
        val current = _uiState.value
        val totalSeconds = current.remainingSeconds
        val startTime = System.currentTimeMillis()

        // Record absolute deadline using elapsedRealtime (immune to system clock changes)
        timerEndElapsed = SystemClock.elapsedRealtime() + (totalSeconds * 1000L)

        // Persist state so it survives process death
        TimerPersistence.saveTimerState(
            application,
            current.state.name,
            timerEndElapsed,
            current.workDuration,
            current.breakDuration,
            current.currentSessionTask
        )

        // Set AlarmManager backup alarm in case process is killed
        val endTimeMillis = startTime + (totalSeconds * 1000L)
        TimerPersistence.scheduleAlarmFallback(application, endTimeMillis)

        timerJob = viewModelScope.launch {
            while (isActive) {
                // Calculate remaining time from real elapsed time
                val remainingMs = timerEndElapsed - SystemClock.elapsedRealtime()

                if (remainingMs <= 0) {
                    // Session complete
                    onTimerComplete(startTime)
                    break
                }

                val remainingSec = (remainingMs / 1000L).toInt()
                _uiState.update { it.copy(remainingSeconds = remainingSec) }

                // Emit tick event if sound is enabled
                if (current.isTickSoundEnabled && current.state != TomatoState.IDLE) {
                    _tickEvent.emit(Unit)
                }

                // Update every 250ms for smoother UI;
                // even if frozen and resumed, elapsedRealtime always gives correct value
                delay(250L)
            }
        }
    }

    /** Called when elapsed time has passed the deadline */
    private suspend fun onTimerComplete(startTime: Long) {
        val current = _uiState.value

        // Clear persisted state
        TimerPersistence.clearTimerState(application)

        if (current.state == TomatoState.WORKING) {
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
        TomatoForegroundService.stopService(application)
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
        // Don't stop service on clear — the service keeps running independently
        // Only clear the persisted state
        if (_uiState.value.state == TomatoState.IDLE) {
            TimerPersistence.clearTimerState(application)
        }
    }
}
