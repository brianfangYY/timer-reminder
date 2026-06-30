package com.timer.reminder.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            // On boot, recreate notification channels
            NotificationHelper.createNotificationChannels(context)

            // TODO: In a production app, reload all active reminders from the database
            // and re-schedule them
        }
    }
}
