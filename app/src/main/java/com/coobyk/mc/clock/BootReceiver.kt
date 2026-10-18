package com.coobyk.mc.clock

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        AlarmStore.load(context).filter { it.enabled }.forEach { AlarmScheduler.schedule(context, it) }
        // reschedule timer if it was running
        if (TimerState.isRunning(context)) {
            val remaining = TimerState.remainingMs(context)
            if (remaining > 0) {
                val trigger = System.currentTimeMillis() + remaining
                val pi = PendingIntent.getBroadcast(
                    context, 9998,
                    Intent(context, TimerReceiver::class.java),
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
                )
                val am = context.getSystemService(Context.ALARM_SERVICE) as android.app.AlarmManager
                am.setExactAndAllowWhileIdle(android.app.AlarmManager.RTC_WAKEUP, trigger, pi)
            }
        }
    }
}