package com.coobyk.mc.clock

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build

object AlarmScheduler {
    fun schedule(context: Context, alarm: Alarm) {
        if (!alarm.enabled) { cancel(context, alarm); return }
        val trigger = alarm.nextTriggerMillis()
        val pi = pendingIntent(context, alarm)
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!am.canScheduleExactAlarms()) return
        }
        val info = AlarmManager.AlarmClockInfo(trigger, null)
        am.setAlarmClock(info, pi)
    }

    fun cancel(context: Context, alarm: Alarm) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        am.cancel(pendingIntent(context, alarm))
    }

    fun scheduleTimer(context: Context, triggerAt: Long) {
        val pi = timerPendingIntent(context)
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !am.canScheduleExactAlarms()) return
        am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi)
    }

    fun cancelTimer(context: Context) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        am.cancel(timerPendingIntent(context))
    }

    private fun pendingIntent(context: Context, alarm: Alarm): PendingIntent {
        val intent = Intent(context, AlarmReceiver::class.java).putExtra("alarm_id", alarm.id)
        val code = (alarm.id % Int.MAX_VALUE).toInt()
        return PendingIntent.getBroadcast(
            context, code, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private fun timerPendingIntent(context: Context): PendingIntent =
        PendingIntent.getBroadcast(
            context, 9998,
            Intent(context, TimerReceiver::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
}