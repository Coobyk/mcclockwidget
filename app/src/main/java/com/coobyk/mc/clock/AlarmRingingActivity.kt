package com.coobyk.mc.clock

import android.content.Context
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.coobyk.mc.clock.ui.theme.AppBg
import com.coobyk.mc.clock.ui.theme.AppSurface
import com.coobyk.mc.clock.ui.theme.AppText
import com.coobyk.mc.clock.ui.theme.AppTextDim
import com.coobyk.mc.clock.ui.theme.McGreen
import com.coobyk.mc.clock.ui.theme.McTheme

class AlarmRingingActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val mode = intent.getStringExtra("mode") ?: "alarm"
        val alarmId = intent.getLongExtra("alarm_id", -1L)
        setContent { McTheme { RingingScreen(this@AlarmRingingActivity, mode, alarmId) } }
    }
}

@Composable
private fun RingingScreen(activity: AlarmRingingActivity, mode: String, alarmId: Long) {
    val alarm = if (mode == "alarm" && alarmId >= 0) AlarmStore.find(activity, alarmId) else null
    val title = if (mode == "timer") "Timer finished" else "Alarm"
    val time = alarm?.timeText() ?: java.time.LocalTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"))
    val label = alarm?.label?.takeIf { it.isNotBlank() } ?: if (mode == "timer") "Time's up!" else ""

    var ringtone by remember { mutableStateOf<android.media.Ringtone?>(null) }
    var vibrator by remember { mutableStateOf<Vibrator?>(null) }

    DisposableEffect(Unit) {
        // start
        try {
            val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val r = RingtoneManager.getRingtone(activity, uri)
            r?.play()
            ringtone = r
        } catch (_: Throwable) {}
        try {
            val v = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                (activity.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager).defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                activity.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            }
            val pattern = longArrayOf(0, 500, 500)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                v.vibrate(VibrationEffect.createWaveform(pattern, 0))
            } else {
                @Suppress("DEPRECATION")
                v.vibrate(pattern, 0)
            }
            vibrator = v
        } catch (_: Throwable) {}

        onDispose {
            try { ringtone?.stop() } catch (_: Throwable) {}
            try { vibrator?.cancel() } catch (_: Throwable) {}
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBg)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(title, color = AppText, fontSize = 18.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
        Text(time, color = AppText, fontSize = 60.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, modifier = Modifier.padding(top = 24.dp))
        if (label.isNotEmpty()) {
            Text(label, color = AppTextDim, fontSize = 14.sp, modifier = Modifier.padding(top = 8.dp))
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 40.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (mode == "alarm") {
                Button(
                    onClick = {
                        alarm?.let {
                            val newTime = System.currentTimeMillis() + it.snoozeMinutes * 60_000L
                            val cal = java.util.Calendar.getInstance().apply { timeInMillis = newTime }
                            val snoozed = it.copy(
                                id = System.currentTimeMillis(),
                                days = 0,
                                hour = cal.get(java.util.Calendar.HOUR_OF_DAY),
                                minute = cal.get(java.util.Calendar.MINUTE),
                                enabled = true,
                            )
                            AlarmStore.save(activity, snoozed)
                            AlarmScheduler.schedule(activity, snoozed)
                        }
                        activity.finish()
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = AppSurface, contentColor = AppText),
                    shape = RoundedCornerShape(8.dp),
                ) {
                    Text("Snooze", fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, fontSize = 13.sp)
                }
            }
            Button(
                onClick = { activity.finish() },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = McGreen, contentColor = AppText),
                shape = RoundedCornerShape(8.dp),
            ) {
                Text("Dismiss", fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, fontSize = 13.sp)
            }
        }
    }
}