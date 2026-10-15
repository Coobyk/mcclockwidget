package com.t3codes.mcclock

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.format.DateFormat
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.t3codes.mcclock.databinding.ActivityMainBinding
import com.t3codes.mcclock.widget.ClockWidgetProvider
import java.time.LocalDateTime
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val handler = Handler(Looper.getMainLooper())
    private val ticker = object : Runnable {
        override fun run() {
            refresh()
            handler.postDelayed(this, 1_000L)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.addWidgetButton.setOnClickListener { pinWidget() }
    }

    override fun onResume() {
        super.onResume()
        refresh()
        handler.postDelayed(ticker, 1_000L)
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(ticker)
    }

    private fun refresh() {
        val now = LocalDateTime.now()
        val dayTime = MinecraftTime.dayTimeFor(now)
        val twentyFourHour = DateFormat.is24HourFormat(this)

        binding.clockView.refresh()
        binding.timeValue.text = digits(now, twentyFourHour, withSeconds = true)
        binding.localValue.text = digits(now, twentyFourHour, withSeconds = false)
        binding.minecraftValue.text = MinecraftTime.worldClockText(dayTime)
        val ticks = dayTime.toInt()
        binding.ticksValue.text = resources.getQuantityString(R.plurals.ticks_value, ticks, ticks)
        binding.frameValue.text = getString(R.string.frame_value, MinecraftTime.clockFrame(dayTime))
        binding.phaseValue.text = MinecraftTime.phase(dayTime).label
    }

    private fun digits(now: LocalDateTime, twentyFourHour: Boolean, withSeconds: Boolean): String {
        val seconds = if (withSeconds) String.format(Locale.US, ":%02d", now.second) else ""
        return if (twentyFourHour) {
            String.format(Locale.US, "%02d:%02d%s", now.hour, now.minute, seconds)
        } else {
            val hour = now.hour % 12
            String.format(
                Locale.US,
                "%d:%02d%s %s",
                if (hour == 0) 12 else hour,
                now.minute,
                seconds,
                if (now.hour < 12) "AM" else "PM",
            )
        }
    }

    private fun pinWidget() {
        val manager = AppWidgetManager.getInstance(this)
        val provider = ComponentName(this, ClockWidgetProvider::class.java)

        if (manager == null || !manager.isRequestPinAppWidgetSupported) {
            Toast.makeText(this, R.string.pin_unsupported, Toast.LENGTH_LONG).show()
            return
        }

        if (!manager.requestPinAppWidget(provider, null, null)) {
            Toast.makeText(this, R.string.pin_unsupported, Toast.LENGTH_LONG).show()
        }
    }
}
