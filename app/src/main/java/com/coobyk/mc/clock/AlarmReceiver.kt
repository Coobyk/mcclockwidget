package com.coobyk.mc.clock

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val id = intent.getLongExtra("alarm_id", -1L)
        if (id < 0) return
        val alarm = AlarmStore.find(context, id) ?: return
        if (alarm.days == 0) {
            alarm.enabled = false
            AlarmStore.save(context, alarm)
        }
        NotificationHelper.showAlarm(context, alarm)
        if (alarm.enabled && alarm.days != 0) AlarmScheduler.schedule(context, alarm)
    }
}