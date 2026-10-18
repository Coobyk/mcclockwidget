package com.coobyk.mc.clock

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class TimerReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        TimerState.reset(context) // clear running state; keep total for display? reset is fine
        NotificationHelper.showTimer(context)
    }
}