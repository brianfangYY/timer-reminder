package com.timer.reminder.service

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.timer.reminder.data.local.entity.ReminderEntity
import java.util.*

/**
 * Schedules and manages Android AlarmManager alarms for reminders.
 *
 * Supports periodic schedules:
 *   - daily:   every day at the same time
 *   - workdays: Monday–Friday at the same time
 *   - weekly:   specific days of the week (e.g. Monday, Wednesday)
 *   - monthly:  specific days of the month (e.g. 1st, 15th)
 */
object ReminderScheduler {

    /**
     * Schedule (or re-schedule) a reminder alarm for its next trigger time.
     */
    fun schedule(context: Context, reminder: ReminderEntity) {
        val nextTrigger = computeNextTrigger(reminder) ?: return
        setAlarm(context, reminder.id, nextTrigger, reminder.title, reminder.description, reminder.linkedTaskId)
    }

    /**
     * Schedule a one-shot reminder (legacy / initial creation).
     */
    fun schedule(
        context: Context,
        reminderId: Long,
        triggerAtMillis: Long,
        title: String = "",
        message: String = "",
        linkedTaskId: Long? = null
    ) {
        setAlarm(context, reminderId, triggerAtMillis, title, message, linkedTaskId)
    }

    /**
     * Given a reminder entity, calculate the next time it should fire.
     * Returns null if the reminder has no valid schedule.
     */
    fun computeNextTrigger(reminder: ReminderEntity): Long? {
        val now = System.currentTimeMillis()
        val cal = Calendar.getInstance()

        when (reminder.repeatType) {
            "none" -> {
                // One-shot: use triggerAtMillis (if still in the future)
                return if (reminder.triggerAtMillis > now) reminder.triggerAtMillis else null
            }

            "daily" -> {
                // Every day at the same time of day
                val timeCal = Calendar.getInstance().apply { timeInMillis = reminder.triggerAtMillis }
                cal.set(Calendar.HOUR_OF_DAY, timeCal.get(Calendar.HOUR_OF_DAY))
                cal.set(Calendar.MINUTE, timeCal.get(Calendar.MINUTE))
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                if (cal.timeInMillis <= now) cal.add(Calendar.DAY_OF_YEAR, 1)
                return cal.timeInMillis
            }

            "workdays" -> {
                // Monday (2) to Friday (6)
                val timeCal = Calendar.getInstance().apply { timeInMillis = reminder.triggerAtMillis }
                cal.set(Calendar.HOUR_OF_DAY, timeCal.get(Calendar.HOUR_OF_DAY))
                cal.set(Calendar.MINUTE, timeCal.get(Calendar.MINUTE))
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)

                // If today is a weekday and the time hasn't passed yet, use today
                val todayDow = cal.get(Calendar.DAY_OF_WEEK)
                if (todayDow in Calendar.MONDAY..Calendar.FRIDAY && cal.timeInMillis > now) {
                    return cal.timeInMillis
                }

                // Otherwise advance to next weekday
                cal.timeInMillis = now
                cal.set(Calendar.HOUR_OF_DAY, timeCal.get(Calendar.HOUR_OF_DAY))
                cal.set(Calendar.MINUTE, timeCal.get(Calendar.MINUTE))
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                while (true) {
                    val dow = cal.get(Calendar.DAY_OF_WEEK)
                    if (dow in Calendar.MONDAY..Calendar.FRIDAY && cal.timeInMillis > now) {
                        return cal.timeInMillis
                    }
                    cal.add(Calendar.DAY_OF_YEAR, 1)
                }
            }

            "weekly" -> {
                val days = reminder.getRepeatDaysOfWeekList()
                if (days.isEmpty()) return null
                val timeCal = Calendar.getInstance().apply { timeInMillis = reminder.triggerAtMillis }
                val targetHour = timeCal.get(Calendar.HOUR_OF_DAY)
                val targetMinute = timeCal.get(Calendar.MINUTE)

                // Try each selected day; find the nearest one
                for (i in 0..7) { // search up to 7 days ahead
                    val testCal = Calendar.getInstance()
                    testCal.add(Calendar.DAY_OF_YEAR, i)
                    val dow = testCal.get(Calendar.DAY_OF_WEEK)
                    if (dow in days) {
                        testCal.set(Calendar.HOUR_OF_DAY, targetHour)
                        testCal.set(Calendar.MINUTE, targetMinute)
                        testCal.set(Calendar.SECOND, 0)
                        testCal.set(Calendar.MILLISECOND, 0)
                        if (testCal.timeInMillis > now) {
                            return testCal.timeInMillis
                        }
                    }
                }
                // Fallback: same day next week
                cal.timeInMillis = now
                cal.add(Calendar.WEEK_OF_YEAR, 1)
                cal.set(Calendar.HOUR_OF_DAY, targetHour)
                cal.set(Calendar.MINUTE, targetMinute)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                // Align to nearest selected day
                val validDays = days.toSet()
                for (offset in 0..6) {
                    val testCal = Calendar.getInstance().apply {
                        timeInMillis = cal.timeInMillis
                        add(Calendar.DAY_OF_YEAR, offset)
                    }
                    if (testCal.get(Calendar.DAY_OF_WEEK) in validDays) {
                        return testCal.timeInMillis
                    }
                }
                return cal.timeInMillis
            }

            "monthly" -> {
                val days = reminder.getRepeatDaysOfMonthList()
                if (days.isEmpty()) return null
                val timeCal = Calendar.getInstance().apply { timeInMillis = reminder.triggerAtMillis }
                val targetHour = timeCal.get(Calendar.HOUR_OF_DAY)
                val targetMinute = timeCal.get(Calendar.MINUTE)

                // Search forward up to 31 days for the next matching day-of-month
                for (i in 0..31) {
                    val testCal = Calendar.getInstance()
                    testCal.add(Calendar.DAY_OF_YEAR, i)
                    val dom = testCal.get(Calendar.DAY_OF_MONTH)
                    if (dom in days) {
                        testCal.set(Calendar.HOUR_OF_DAY, targetHour)
                        testCal.set(Calendar.MINUTE, targetMinute)
                        testCal.set(Calendar.SECOND, 0)
                        testCal.set(Calendar.MILLISECOND, 0)
                        if (testCal.timeInMillis > now) {
                            return testCal.timeInMillis
                        }
                    }
                }
                return null
            }

            else -> {
                // Unknown type — treat as one-shot
                return if (reminder.triggerAtMillis > now) reminder.triggerAtMillis else null
            }
        }
    }

    /**
     * Re-schedule a repeating reminder after it fires.
     * Computes the next trigger and sets a new alarm; if no more triggers exist,
     * the old alarm is cancelled.
     */
    fun rescheduleAfterFiring(context: Context, reminder: ReminderEntity) {
        val next = computeNextTrigger(reminder)
        if (next != null) {
            setAlarm(context, reminder.id, next, reminder.title, reminder.description, reminder.linkedTaskId)
        }
    }

    fun cancel(context: Context, reminderId: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            (1000 + reminderId).toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()
    }

    private fun setAlarm(
        context: Context,
        reminderId: Long,
        triggerAtMillis: Long,
        title: String,
        message: String,
        linkedTaskId: Long? = null
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("reminder_id", reminderId)
            putExtra("title", title)
            putExtra("message", message)
            putExtra("type", "提醒")
            if (linkedTaskId != null) putExtra("linked_task_id", linkedTaskId)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            (1000 + reminderId).toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            } else {
                alarmManager.setAlarmClock(
                    AlarmManager.AlarmClockInfo(triggerAtMillis, pendingIntent),
                    pendingIntent
                )
            }
        } else {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent
            )
        }
    }
}
