package com.coobyk.mc.clock.widget

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build

/**
 * Wakes the widget up just after every minute boundary.
 *
 * `RTC` (not `RTC_WAKEUP`) is deliberate: a home screen widget is only on screen while the device
 * is awake anyway, so an alarm that waits for the device is both accurate enough and battery
 * friendly. Exact alarms are used when the platform allows them and we fall back to an inexact
 * alarm otherwise.
 */
object WidgetScheduler {

    private const val REQUEST_CODE = 4201

    fun schedule(context: Context) {
        val manager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val triggerAt = (System.currentTimeMillis() / 60_000L + 1L) * 60_000L + 750L
        val pending = pendingIntent(context)

        try {
            if (canScheduleExact(manager)) {
                manager.setExact(AlarmManager.RTC, triggerAt, pending)
            } else {
                manager.set(AlarmManager.RTC, triggerAt, pending)
            }
        } catch (t: SecurityException) {
            manager.set(AlarmManager.RTC, triggerAt, pending)
        }
    }

    fun cancel(context: Context) {
        val manager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        manager.cancel(pendingIntent(context))
    }

    private fun canScheduleExact(manager: AlarmManager): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.S || manager.canScheduleExactAlarms()

    private fun pendingIntent(context: Context): PendingIntent {
        val intent = Intent(context, ClockWidgetProvider::class.java)
            .setAction(ClockWidgetProvider.ACTION_TICK)
        return PendingIntent.getBroadcast(
            context,
            REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }
}
