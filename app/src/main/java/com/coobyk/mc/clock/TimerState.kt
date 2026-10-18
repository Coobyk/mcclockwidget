package com.coobyk.mc.clock

import android.content.Context

object TimerState {
    private const val PREFS = "mc_timer"
    private const val KEY_END = "end_at"
    private const val KEY_TOTAL = "total_ms"
    private const val KEY_RUNNING = "running"

    fun endAt(context: Context): Long =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getLong(KEY_END, 0L)

    fun totalMs(context: Context): Long =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getLong(KEY_TOTAL, 0L)

    fun isRunning(context: Context): Boolean =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getBoolean(KEY_RUNNING, false)

    fun start(context: Context, durationMs: Long) {
        val now = System.currentTimeMillis()
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putLong(KEY_END, now + durationMs)
            .putLong(KEY_TOTAL, durationMs)
            .putBoolean(KEY_RUNNING, true)
            .apply()
    }

    fun pause(context: Context) {
        val remaining = (endAt(context) - System.currentTimeMillis()).coerceAtLeast(0L)
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putLong(KEY_END, System.currentTimeMillis() + remaining)
            .putLong(KEY_TOTAL, remaining)
            .putBoolean(KEY_RUNNING, false)
            .apply()
    }

    fun resume(context: Context) {
        val remaining = totalMs(context)
        val now = System.currentTimeMillis()
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putLong(KEY_END, now + remaining)
            .putBoolean(KEY_RUNNING, true)
            .apply()
    }

    fun reset(context: Context) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putLong(KEY_END, 0L).putLong(KEY_TOTAL, 0L).putBoolean(KEY_RUNNING, false).apply()
    }

    fun remainingMs(context: Context): Long {
        if (!isRunning(context)) return totalMs(context)
        return (endAt(context) - System.currentTimeMillis()).coerceAtLeast(0L)
    }
}