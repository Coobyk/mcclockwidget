package com.coobyk.mc.clock

import org.json.JSONObject
import java.util.Calendar

data class Alarm(
    var id: Long = 0L,
    var hour: Int = 7,
    var minute: Int = 0,
    var label: String = "",
    var enabled: Boolean = true,
    var days: Int = 0, // bitmask: bit (dayOfWeek-1), Sunday=bit0
    var snoozeMinutes: Int = 10,
) {
    fun nextTriggerMillis(from: Long = System.currentTimeMillis()): Long {
        for (offset in 0..7) {
            val c = Calendar.getInstance().apply {
                timeInMillis = from
                add(Calendar.DAY_OF_YEAR, offset)
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            if (c.timeInMillis <= from) continue
            val dayBit = 1 shl (c.get(Calendar.DAY_OF_WEEK) - 1)
            if (days == 0 || (days and dayBit) != 0) return c.timeInMillis
        }
        val c = Calendar.getInstance().apply {
            timeInMillis = from
            add(Calendar.DAY_OF_YEAR, 1)
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return c.timeInMillis
    }

    fun timeText(): String = String.format(java.util.Locale.US, "%02d:%02d", hour, minute)

    fun daysText(): String {
        if (days == 0) return "Once"
        val names = arrayOf("Su", "Mo", "Tu", "We", "Th", "Fr", "Sa")
        val sb = StringBuilder()
        for (i in 0..6) if ((days and (1 shl i)) != 0) {
            if (sb.isNotEmpty()) sb.append(' ')
            sb.append(names[i])
        }
        return if (sb.isEmpty()) "Once" else sb.toString()
    }

    fun toJson(): JSONObject = JSONObject().apply {
        put("id", id); put("hour", hour); put("minute", minute)
        put("label", label); put("enabled", enabled); put("days", days)
        put("snooze", snoozeMinutes)
    }

    companion object {
        fun fromJson(o: JSONObject) = Alarm(
            id = o.optLong("id", System.currentTimeMillis()),
            hour = o.optInt("hour", 7),
            minute = o.optInt("minute", 0),
            label = o.optString("label", ""),
            enabled = o.optBoolean("enabled", true),
            days = o.optInt("days", 0),
            snoozeMinutes = o.optInt("snooze", 10),
        )
    }
}