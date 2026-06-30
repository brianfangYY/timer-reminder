package com.timer.reminder.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat

class TomatoForegroundService : Service() {

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createNotificationChannels(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = NotificationCompat.Builder(this, NotificationHelper.CHANNEL_TOMATO)
            .setContentTitle("番茄钟进行中")
            .setContentText("正在专注工作...")
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setOngoing(true)
            .build()

        startForeground(NotificationHelper.NOTIFICATION_ID_TOMATO_FOREGROUND, notification)
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        stopForeground(true)
        super.onDestroy()
    }
}
