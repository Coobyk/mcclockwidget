package com.t3codes.mcclock.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.format.DateFormat
import android.util.TypedValue
import android.widget.RemoteViews
import com.t3codes.mcclock.ClockArt
import com.t3codes.mcclock.MainActivity
import com.t3codes.mcclock.MinecraftTime
import com.t3codes.mcclock.R
import java.time.LocalDateTime
import java.util.Locale

/**
 * The home screen widget: Minecraft's clock item plus the time as digits.
 *
 * Two layouts are used so the dial sits above the digits on a square widget and beside them on a
 * wide one.
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

        const val ACTION_TICK = "com.t3codes.mcclock.action.TICK"

        fun refreshAll(context: Context) {
            val manager = AppWidgetManager.getInstance(context) ?: return
            val ids = manager.getAppWidgetIds(ComponentName(context, ClockWidgetProvider::class.java))
            ids.forEach { updateWidget(context, manager, it) }
        }

        fun updateWidget(context: Context, manager: AppWidgetManager, appWidgetId: Int) {
            val options = manager.getAppWidgetOptions(appWidgetId)
            val minWidth = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, 110)
            val minHeight = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, 110)

            manager.updateAppWidget(appWidgetId, buildRemoteViews(context, minWidth, minHeight))
        }

        /**
         * Everything the widget shows, for a widget whose current (minimum) size is
         * [minWidthDp] x [minHeightDp]. Kept pure so a test can render it.
         */
        fun buildRemoteViews(context: Context, minWidthDp: Int, minHeightDp: Int): RemoteViews {
            val now = LocalDateTime.now()
            val dayTime = MinecraftTime.dayTimeFor(now)
            val frame = MinecraftTime.clockFrame(dayTime)
            val time = digitalTime(context, now)
            val wide = minWidthDp > minHeightDp * 1.25f

            val description = context.getString(
                R.string.widget_a11y,
                time,
                MinecraftTime.phase(dayTime).label,
            )

            val layout = if (wide) R.layout.widget_clock_wide else R.layout.widget_clock

            val views = RemoteViews(context.packageName, layout)
            views.setImageViewResource(R.id.clock_dial, ClockArt.frameRes(frame))
            views.setTextViewText(R.id.clock_time, time)
            views.setTextViewTextSize(
                R.id.clock_time,
                TypedValue.COMPLEX_UNIT_PX,
                timeSizePx(context, minWidthDp, wide),
            )
            views.setContentDescription(R.id.widget_root, description)
            views.setContentDescription(R.id.clock_dial, description)
            views.setOnClickPendingIntent(R.id.widget_root, openApp(context))
            return views
        }

        /**
         * Silkscreen advances about 0.75em per digit, and the widest thing we ever print is five
         * characters ("23:59"), so the size that fits is a straight division. The size is worked
         * out here rather than left to `autoSizeTextType`, which behaved differently from one
         * launcher to the next and quietly fell back to the default 14sp.
         */
        private fun timeSizePx(context: Context, minWidthDp: Int, wide: Boolean): Float {
            val metrics = context.resources.displayMetrics
            val padding = 10f * metrics.density
            val available = if (wide) {
                (minWidthDp * metrics.density - 2f * padding) / 2f - 8f * metrics.density
            } else {
                minWidthDp * metrics.density - 2f * padding
            }
            return (available * 0.92f / (MAX_DIGITS * ADVANCE_EM))
                .coerceIn(8f * metrics.density, 34f * metrics.density)
        }

        private const val MAX_DIGITS = 5f
        private const val ADVANCE_EM = 0.75f

        /**
         * Digits only: "6:12 PM" would leave the widget far less room for the time, and the dial
         * already says whether it is day or night. The app screen, which has room, adds the
         * meridiem.
         */
        private fun digitalTime(context: Context, now: LocalDateTime): String =
            if (DateFormat.is24HourFormat(context)) {
                String.format(Locale.US, "%02d:%02d", now.hour, now.minute)
            } else {
                val hour = now.hour % 12
                String.format(Locale.US, "%d:%02d", if (hour == 0) 12 else hour, now.minute)
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
