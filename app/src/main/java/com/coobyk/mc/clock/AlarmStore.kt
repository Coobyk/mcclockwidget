package com.coobyk.mc.clock

import android.content.Context
import org.json.JSONArray

object AlarmStore {
    private const val PREFS = "mc_alarms"
    private const val KEY = "alarms_json"

    fun load(context: Context): MutableList<Alarm> {
        val raw = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY, "[]") ?: "[]"
        val out = mutableListOf<Alarm>()
        try {
            val arr = JSONArray(raw)
            for (i in 0 until arr.length()) out.add(Alarm.fromJson(arr.getJSONObject(i)))
        } catch (_: Throwable) {}
        return out
    }

    fun saveAll(context: Context, alarms: List<Alarm>) {
        val arr = JSONArray()
        alarms.forEach { arr.put(it.toJson()) }
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putString(KEY, arr.toString()).apply()
    }

    fun save(context: Context, alarm: Alarm) {
        val list = load(context)
        val idx = list.indexOfFirst { it.id == alarm.id }
        if (idx >= 0) list[idx] = alarm else list.add(alarm)
        saveAll(context, list)
    }

    fun delete(context: Context, id: Long) {
        saveAll(context, load(context).filter { it.id != id })
    }

    fun find(context: Context, id: Long): Alarm? = load(context).firstOrNull { it.id == id }
}