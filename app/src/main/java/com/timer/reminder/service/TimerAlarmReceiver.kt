package com.timer.reminder.service

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.timer.reminder.util.TimerPersistence

/**
 * Backup alarm receiver that fires when the tomato timer expires.
 * This ensures the user gets notified even if the app process was killed.
 */
class TimerAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_TIMER_EXPIRED) return

        // Clear saved state since timer has expired
        TimerPersistence.clearTimerState(context)

        // Post a notification so the user knows
        val notification = NotificationCompat.Builder(
            context, NotificationHelper.CHANNEL_TOMATO
        )
            .setContentTitle("🍅 番茄钟完成！")
            .setContentText("计时已结束")
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        val notificationManager =
            context.getSystemService(NotificationManager::class.java)
        notificationManager.notify(1005, notification)
    }

    companion object {
        const val ACTION_TIMER_EXPIRED = "com.timer.reminder.TIMER_EXPIRED"
        const val REQ_CODE = 9876
    }
}
