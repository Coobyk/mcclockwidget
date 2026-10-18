package com.coobyk.mc.clock.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.format.DateFormat
import android.widget.RemoteViews
import com.coobyk.mc.clock.ClockArt
import com.coobyk.mc.clock.MainActivity
import com.coobyk.mc.clock.MinecraftTime
import com.coobyk.mc.clock.R
import java.time.LocalDateTime
import java.util.Locale

/**
 * The home screen widget: Minecraft's clock item, drawn as large as the widget allows.
 *
 * There is no panel behind it and no digital readout, so the dial fills the whole widget whatever
 * size the user drags it to. The exact time is still readable by a screen reader: it lives in the
 * content description instead of on screen.
 */
class ClockWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, manager: AppWidgetManager, appWidgetIds: IntArray) {
        appWidgetIds.forEach { updateWidget(context, manager, it) }
        WidgetScheduler.schedule(context)
    }

    override fun onAppWidgetOptionsChanged(
        context: Context,
        manager: AppWidgetManager,
        appWidgetId: Int,
        newOptions: Bundle?,
    ) {
        updateWidget(context, manager, appWidgetId)
    }

    override fun onEnabled(context: Context) {
        WidgetScheduler.schedule(context)
    }

    override fun onDisabled(context: Context) {
        WidgetScheduler.cancel(context)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        when (intent.action) {
            ACTION_TICK,
            Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED,
            Intent.ACTION_DATE_CHANGED,
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED,
            -> {
                refreshAll(context)
                WidgetScheduler.schedule(context)
            }
        }
    }

    companion object {

        const val ACTION_TICK = "com.coobyk.mc.clock.action.TICK"

        fun refreshAll(context: Context) {
            val manager = AppWidgetManager.getInstance(context) ?: return
            val ids = manager.getAppWidgetIds(ComponentName(context, ClockWidgetProvider::class.java))
            ids.forEach { updateWidget(context, manager, it) }
        }

        fun updateWidget(context: Context, manager: AppWidgetManager, appWidgetId: Int) {
            manager.updateAppWidget(appWidgetId, buildRemoteViews(context))
        }

        /** Everything the widget shows. Kept pure so a test can render it. */
        fun buildRemoteViews(context: Context): RemoteViews {
            val now = LocalDateTime.now()
            val dayTime = MinecraftTime.dayTimeFor(now)
            val frame = MinecraftTime.clockFrame(dayTime)

            val views = RemoteViews(context.packageName, R.layout.widget_clock)
            views.setImageViewResource(R.id.clock_dial, ClockArt.frameRes(frame))
            views.setContentDescription(
                R.id.widget_root,
                context.getString(
                    R.string.widget_a11y,
                    digitalTime(context, now),
                    MinecraftTime.phase(dayTime).label,
                ),
            )
            views.setOnClickPendingIntent(R.id.widget_root, openApp(context))
            return views
        }

        /** The widget only speaks it: the dial shows the time, this is what TalkBack says. */
        private fun digitalTime(context: Context, now: LocalDateTime): String =
            if (DateFormat.is24HourFormat(context)) {
                String.format(Locale.US, "%02d:%02d", now.hour, now.minute)
            } else {
                val hour = now.hour % 12
                String.format(Locale.US, "%d:%02d %s",
                    if (hour == 0) 12 else hour,
                    now.minute,
                    if (now.hour < 12) "AM" else "PM",
                )
            }

        private fun openApp(context: Context): PendingIntent {
            val intent = Intent(context, MainActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            return PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
        }
    }
}
