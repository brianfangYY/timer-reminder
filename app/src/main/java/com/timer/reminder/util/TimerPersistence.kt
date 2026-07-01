package com.timer.reminder.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.SystemClock
import androidx.core.content.getSystemService
import com.timer.reminder.service.TimerAlarmReceiver

/**
 * Saves/restores timer state across process death.
 *
 * When the Android system kills the app process (task bar icon disappears),
 * all in-memory state is lost. This class persists the timer's end time
 * so it can be resumed correctly when the user comes back.
 */
object TimerPersistence {

    private const val PREFS_NAME = "timer_state"
    private const val KEY_STATE = "state"           // "WORKING" | "BREAK" | ""
    private const val KEY_END_ELAPSED = "end_elapsed" // SystemClock.elapsedRealtime() deadline
    private const val KEY_DURATION = "work_duration"
    private const val KEY_BREAK_DURATION = "break_duration"
    private const val KEY_TASK = "task"

    private fun prefs(ctx: Context): SharedPreferences =
        ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    data class SavedTimerState(
        val stateName: String,       // "WORKING" or "BREAK"
        val endElapsed: Long,        // SystemClock.elapsedRealtime() deadline
        val workDuration: Int,
        val breakDuration: Int,
        val taskDescription: String
    )

    /** Save current running timer state to disk */
    fun saveTimerState(
        ctx: Context,
        stateName: String,
        endElapsedMs: Long,
        workDuration: Int,
        breakDuration: Int,
        taskDescription: String
    ) {
        prefs(ctx).edit()
            .putString(KEY_STATE, stateName)
            .putLong(KEY_END_ELAPSED, endElapsedMs)
            .putInt(KEY_DURATION, workDuration)
            .putInt(KEY_BREAK_DURATION, breakDuration)
            .putString(KEY_TASK, taskDescription)
            .apply()
    }

    /** Try to restore saved timer state; returns null if no saved state or expired */
    fun restoreTimerState(ctx: Context): SavedTimerState? {
        val prefs = prefs(ctx)
        val stateName = prefs.getString(KEY_STATE, "") ?: ""
        if (stateName.isEmpty()) return null

        val endElapsed = prefs.getLong(KEY_END_ELAPSED, 0L)
        if (endElapsed <= 0) return null

        // If the timer has already expired, clean up
        if (SystemClock.elapsedRealtime() >= endElapsed) {
            clearTimerState(ctx)
            return null
        }

        return SavedTimerState(
            stateName = stateName,
            endElapsed = endElapsed,
            workDuration = prefs.getInt(KEY_DURATION, 25),
            breakDuration = prefs.getInt(KEY_BREAK_DURATION, 5),
            taskDescription = prefs.getString(KEY_TASK, "") ?: ""
        )
    }

    /** Clear saved timer state */
    fun clearTimerState(ctx: Context) {
        prefs(ctx).edit()
            .remove(KEY_STATE)
            .remove(KEY_END_ELAPSED)
            .remove(KEY_DURATION)
            .remove(KEY_BREAK_DURATION)
            .remove(KEY_TASK)
            .apply()
        cancelAlarmFallback(ctx)
    }

    /** Schedule an AlarmManager backup alarm for when the timer expires */
    fun scheduleAlarmFallback(ctx: Context, endTimeMillis: Long) {
        val alarmManager = ctx.getSystemService<AlarmManager>() ?: return
        val intent = Intent(ctx, TimerAlarmReceiver::class.java).apply {
            action = TimerAlarmReceiver.ACTION_TIMER_EXPIRED
        }
        val pendingIntent = PendingIntent.getBroadcast(
            ctx, TimerAlarmReceiver.REQ_CODE, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP, endTimeMillis, pendingIntent
        )
    }

    /** Cancel the backup alarm */
    private fun cancelAlarmFallback(ctx: Context) {
        val alarmManager = ctx.getSystemService<AlarmManager>() ?: return
        val intent = Intent(ctx, TimerAlarmReceiver::class.java).apply {
            action = TimerAlarmReceiver.ACTION_TIMER_EXPIRED
        }
        val pendingIntent = PendingIntent.getBroadcast(
            ctx, TimerAlarmReceiver.REQ_CODE, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }
}
