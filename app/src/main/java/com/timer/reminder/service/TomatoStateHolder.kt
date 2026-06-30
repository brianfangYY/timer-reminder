package com.timer.reminder.service

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Singleton that bridges TomatoForegroundService and TomatoViewModel.
 * The service writes here, the ViewModel reads from here.
 */
object TomatoStateHolder {

    private val _remainingSeconds = MutableStateFlow(0L)
    val remainingSeconds: StateFlow<Long> = _remainingSeconds.asStateFlow()

    private val _isServiceRunning = MutableStateFlow(false)
    val isServiceRunning: StateFlow<Boolean> = _isServiceRunning.asStateFlow()

    private val _phase = MutableStateFlow("idle") // idle | focus | break
    val phase: StateFlow<String> = _phase.asStateFlow()

    fun updateTime(seconds: Long) {
        _remainingSeconds.value = seconds
    }

    fun setPhase(p: String) {
        _phase.value = p
    }

    fun setServiceRunning(running: Boolean) {
        _isServiceRunning.value = running
    }

    fun onTimerComplete() {
        _phase.value = "idle"
        _remainingSeconds.value = 0L
    }

    fun reset() {
        _remainingSeconds.value = 0L
        _phase.value = "idle"
        _isServiceRunning.value = false
    }
}
