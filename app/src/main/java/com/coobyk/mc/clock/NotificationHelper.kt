package com.coobyk.mc.clock

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

object NotificationHelper {
    const val CHANNEL_ALARMS = "mc_alarms"
    const val CHANNEL_TIMERS = "mc_timers"

    fun ensureChannels(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (nm.getNotificationChannel(CHANNEL_ALARMS) == null) {
            nm.createNotificationChannel(NotificationChannel(CHANNEL_ALARMS, "Alarms", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "Alarm ringing"
                setSound(android.provider.Settings.System.DEFAULT_ALARM_ALERT_URI, null)
                enableVibration(true)
            })
        }
        if (nm.getNotificationChannel(CHANNEL_TIMERS) == null) {
            nm.createNotificationChannel(NotificationChannel(CHANNEL_TIMERS, "Timers", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "Timer finished"
                setSound(android.provider.Settings.System.DEFAULT_ALARM_ALERT_URI, null)
                enableVibration(true)
            })
        }
    }

    fun showAlarm(context: Context, alarm: Alarm) {
        ensureChannels(context)
        val fullScreen = Intent(context, AlarmRingingActivity::class.java).apply {
            putExtra("mode", "alarm")
            putExtra("alarm_id", alarm.id)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val fsPi = PendingIntent.getActivity(
            context, alarm.id.toInt(), fullScreen,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val n = NotificationCompat.Builder(context, CHANNEL_ALARMS)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(if (alarm.label.isBlank()) "Alarm" else alarm.label)
            .setContentText(alarm.timeText())
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setFullScreenIntent(fsPi, true)
            .setAutoCancel(true)
            .build()
        try { NotificationManagerCompat.from(context).notify(alarm.id.toInt(), n) } catch (_: SecurityException) {}
        // Best-effort direct launch (works in foreground / on older Android).
        try { context.startActivity(fullScreen) } catch (_: Throwable) {}
    }

    fun showTimer(context: Context) {
        ensureChannels(context)
        val fullScreen = Intent(context, AlarmRingingActivity::class.java).apply {
            putExtra("mode", "timer")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val fsPi = PendingIntent.getActivity(
            context, 9999, fullScreen,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val n = NotificationCompat.Builder(context, CHANNEL_TIMERS)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Timer finished")
            .setContentText("Time's up!")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setFullScreenIntent(fsPi, true)
            .setAutoCancel(true)
            .build()
        try { NotificationManagerCompat.from(context).notify(9999, n) } catch (_: SecurityException) {}
        try { context.startActivity(fullScreen) } catch (_: Throwable) {}
    }
}