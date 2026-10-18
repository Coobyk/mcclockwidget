package com.coobyk.mc.clock

import android.content.Context
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.coobyk.mc.clock.databinding.ActivityAlarmRingingBinding

class AlarmRingingActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAlarmRingingBinding
    private var ringtone: android.media.Ringtone? = null
    private var vibrator: Vibrator? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAlarmRingingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val mode = intent.getStringExtra("mode") ?: "alarm"
        val alarmId = intent.getLongExtra("alarm_id", -1L)
        val alarm = if (mode == "alarm" && alarmId >= 0) AlarmStore.find(this, alarmId) else null

        binding.titleText.text = if (mode == "timer") "Timer finished" else "Alarm"
        binding.timeText.text = alarm?.timeText() ?: java.time.LocalTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"))
        binding.labelText.text = alarm?.label?.takeIf { it.isNotBlank() } ?: if (mode == "timer") "Time's up!" else ""

        binding.snoozeButton.visibility = if (mode == "alarm") android.view.View.VISIBLE else android.view.View.GONE

        binding.dismissButton.setOnClickListener { stopRinging(); finish() }
        binding.snoozeButton.setOnClickListener {
            alarm?.let {
                val newTime = System.currentTimeMillis() + it.snoozeMinutes * 60_000L
                // schedule a one-shot alarm at snooze time by creating a temp alarm
                val snoozed = it.copy(id = System.currentTimeMillis(), days = 0, hour = java.util.Calendar.getInstance().apply { timeInMillis = newTime }.get(java.util.Calendar.HOUR_OF_DAY), minute = java.util.Calendar.getInstance().apply { timeInMillis = newTime }.get(java.util.Calendar.MINUTE), enabled = true)
                AlarmStore.save(this, snoozed)
                AlarmScheduler.schedule(this, snoozed)
            }
            stopRinging(); finish()
        }

        startRinging()
    }

    private fun startRinging() {
        try {
            val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            ringtone = RingtoneManager.getRingtone(this, uri)
            ringtone?.play()
        } catch (_: Throwable) {}
        try {
            vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                (getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager).defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            }
            val pattern = longArrayOf(0, 500, 500)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createWaveform(pattern, 0))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(pattern, 0)
            }
        } catch (_: Throwable) {}
    }

    private fun stopRinging() {
        try { ringtone?.stop() } catch (_: Throwable) {}
        try { vibrator?.cancel() } catch (_: Throwable) {}
    }

    override fun onDestroy() {
        stopRinging()
        super.onDestroy()
    }
}