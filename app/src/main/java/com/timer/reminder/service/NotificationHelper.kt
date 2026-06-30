package com.timer.reminder.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.timer.reminder.R

object NotificationHelper {

    const val CHANNEL_REMINDER = "timer_reminder_channel"
    const val CHANNEL_TOMATO = "tomato_timer_channel"
    const val CHANNEL_ALARM = "alarm_channel"

    const val NOTIFICATION_ID_REMINDER = 1001
    const val NOTIFICATION_ID_TOMATO = 1002
    const val NOTIFICATION_ID_ALARM = 1003
    const val NOTIFICATION_ID_TOMATO_FOREGROUND = 1004

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channels = listOf(
                NotificationChannel(
                    CHANNEL_REMINDER,
                    "提醒通知",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "定时提醒通知"
                },
                NotificationChannel(
                    CHANNEL_TOMATO,
                    "番茄钟",
                    NotificationManager.IMPORTANCE_LOW
                ).apply {
                    description = "番茄钟状态通知"
                },
                NotificationChannel(
                    CHANNEL_ALARM,
                    "闹钟",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "闹钟响铃通知"
                }
            )

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            channels.forEach { notificationManager.createNotificationChannel(it) }
        }
    }

    fun showReminderNotification(context: Context, title: String, message: String) {
        val notification = NotificationCompat.Builder(context, CHANNEL_REMINDER)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID_REMINDER, notification)
    }

    fun showAlarmNotification(context: Context, label: String) {
        val notification = NotificationCompat.Builder(context, CHANNEL_ALARM)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("闹钟")
            .setContentText(if (label.isNotEmpty()) label else "闹钟时间到了！")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID_ALARM, notification)
    }
}
