package com.timer.reminder.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val alarmId = intent.getLongExtra("alarm_id", -1L)
        val alarmLabel = intent.getStringExtra("alarm_label") ?: ""
        val reminderId = intent.getLongExtra("reminder_id", -1L)

        if (alarmId != -1L) {
            NotificationHelper.showAlarmNotification(context, alarmLabel)
        } else if (reminderId != -1L) {
            NotificationHelper.showReminderNotification(
                context,
                "提醒",
                "您有一条新的提醒"
            )
        }
    }
}
