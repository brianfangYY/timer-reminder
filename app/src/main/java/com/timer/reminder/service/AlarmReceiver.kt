package com.timer.reminder.service

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.timer.reminder.ui.reminder.ReminderAlertActivity

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val reminderId = intent.getLongExtra("reminder_id", -1L)
        val title = intent.getStringExtra("title") ?: "小叮当提醒"
        val message = intent.getStringExtra("message") ?: "时间到了！"
        val type = intent.getStringExtra("type") ?: "提醒"
        val linkedTaskId = intent.getLongExtra("linked_task_id", -1L)

        // 启动全屏提醒 Activity
        val fullIntent = Intent(context, ReminderAlertActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("reminder_id", reminderId)
            putExtra("title", title)
            putExtra("message", message)
            putExtra("type", type)
            putExtra("linked_task_id", linkedTaskId)
        }

        // Android 12+ 需要显示通知才能从后台启动 Activity
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            showUrgentNotification(context, reminderId, title, message, fullIntent)
        }

        context.startActivity(fullIntent)
    }

    private fun showUrgentNotification(
        context: Context,
        id: Long,
        title: String,
        message: String,
        fullIntent: Intent
    ) {
        val pendingIntent = PendingIntent.getActivity(
            context, id.toInt(), fullIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(context, NotificationHelper.CHANNEL_REMINDER)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setFullScreenIntent(pendingIntent, true)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(id.toInt(), notification)
        } catch (e: SecurityException) {
            // 如果没有通知权限，直接启动 Activity
            context.startActivity(fullIntent)
        }
    }
}
