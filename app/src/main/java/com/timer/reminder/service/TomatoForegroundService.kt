package com.timer.reminder.service

import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.CountDownTimer
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.timer.reminder.MainActivity

class TomatoForegroundService : Service() {

    private var timer: CountDownTimer? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createNotificationChannels(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val durationSeconds = intent.getLongExtra(EXTRA_DURATION_SECONDS, 25 * 60L)
                val phase = intent.getStringExtra(EXTRA_PHASE) ?: "focus"
                TomatoStateHolder.setPhase(phase)
                TomatoStateHolder.setServiceRunning(true)

                startForeground(NOTIFICATION_ID, createNotification("准备中...", phase))
                startTimer(durationSeconds, phase)
            }
            ACTION_UPDATE_TIME -> {
                // Update notification with current time from ViewModel
                val remaining = intent.getLongExtra(EXTRA_REMAINING_SECONDS, 0L)
                val currentPhase = TomatoStateHolder.phase.value
                updateNotification(remaining, currentPhase)
            }
            ACTION_STOP -> {
                stopTimer()
                TomatoStateHolder.onTimerComplete()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }
        return START_STICKY
    }

    private fun startTimer(totalSeconds: Long, phase: String) {
        timer?.cancel()
        TomatoStateHolder.updateTime(totalSeconds)

        timer = object : CountDownTimer(totalSeconds * 1000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val remaining = millisUntilFinished / 1000
                TomatoStateHolder.updateTime(remaining)
                updateNotification(remaining, phase)
            }

            override fun onFinish() {
                TomatoStateHolder.updateTime(0)
                TomatoStateHolder.onTimerComplete()

                val completionNotification = NotificationCompat.Builder(this@TomatoForegroundService, NotificationHelper.CHANNEL_TOMATO)
                    .setContentTitle("🍅 番茄钟完成！")
                    .setContentText(if (phase == "focus") "专注时间结束" else "休息时间结束")
                    .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
                    .setAutoCancel(true)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .build()

                val notificationManager = getSystemService(NotificationManager::class.java)
                notificationManager.notify(NOTIFICATION_ID, completionNotification)

                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }.start()
    }

    private fun stopTimer() {
        timer?.cancel()
        timer = null
    }

    private fun updateNotification(remainingSeconds: Long, phase: String) {
        val timeText = formatTime(remainingSeconds)
        val notification = createNotification(timeText, phase)
        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(NOTIFICATION_ID, notification)
    }

    private fun createNotification(timeText: String, phase: String): android.app.Notification {
        val title = when (phase) {
            "focus" -> "🍅 专注中"
            "break" -> "☕ 休息中"
            else -> "🍅 番茄钟"
        }

        // Stop action
        val stopIntent = Intent(this, TomatoForegroundService::class.java).apply {
            action = ACTION_STOP
        }
        val stopPendingIntent = PendingIntent.getService(
            this, 0, stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Open app action
        val openIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val openPendingIntent = PendingIntent.getActivity(
            this, 1, openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, NotificationHelper.CHANNEL_TOMATO)
            .setContentTitle(title)
            .setContentText("剩余时间: $timeText")
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(openPendingIntent)
            .addAction(android.R.drawable.ic_media_pause, "停止", stopPendingIntent)
            .build()
    }

    private fun formatTime(seconds: Long): String {
        val m = seconds / 60
        val s = seconds % 60
        return "%02d:%02d".format(m, s)
    }

    override fun onDestroy() {
        stopTimer()
        TomatoStateHolder.onTimerComplete()
        super.onDestroy()
    }

    companion object {
        const val NOTIFICATION_ID = 1004
        const val ACTION_START = "TOMATO_START"
        const val ACTION_STOP = "TOMATO_STOP"
        const val ACTION_UPDATE_TIME = "TOMATO_UPDATE_TIME"
        const val EXTRA_DURATION_SECONDS = "extra_duration_seconds"
        const val EXTRA_PHASE = "extra_phase"
        const val EXTRA_REMAINING_SECONDS = "extra_remaining_seconds"

        fun startService(context: Context, durationSeconds: Long, phase: String = "focus") {
            val intent = Intent(context, TomatoForegroundService::class.java).apply {
                action = ACTION_START
                putExtra(EXTRA_DURATION_SECONDS, durationSeconds)
                putExtra(EXTRA_PHASE, phase)
            }
            context.startForegroundService(intent)
        }

        fun stopService(context: Context) {
            val intent = Intent(context, TomatoForegroundService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }

        fun updateNotification(context: Context, remainingSeconds: Long, phase: String) {
            val intent = Intent(context, TomatoForegroundService::class.java).apply {
                action = ACTION_UPDATE_TIME
                putExtra(EXTRA_REMAINING_SECONDS, remainingSeconds)
                putExtra(EXTRA_PHASE, phase)
            }
            context.startService(intent)
        }
    }
}
