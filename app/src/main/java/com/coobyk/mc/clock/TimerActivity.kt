package com.coobyk.mc.clock

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.coobyk.mc.clock.databinding.ActivityTimerBinding

class TimerActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTimerBinding
    private val handler = Handler(Looper.getMainLooper())
    private val ticker = object : Runnable {
        override fun run() {
            refresh()
            if (TimerState.isRunning(this@TimerActivity)) handler.postDelayed(this, 250L)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTimerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.startButton.setOnClickListener { start() }
        binding.pauseButton.setOnClickListener { pause() }
        binding.resetButton.setOnClickListener { reset() }
        refresh()
        if (TimerState.isRunning(this)) handler.postDelayed(ticker, 250L)
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(ticker)
    }

    override fun onResume() {
        super.onResume()
        refresh()
        if (TimerState.isRunning(this)) handler.postDelayed(ticker, 250L)
    }

    private fun start() {
        if (TimerState.isRunning(this)) return
        val remaining = TimerState.remainingMs(this)
        if (remaining > 0 && TimerState.endAt(this) > 0L) {
            TimerState.resume(this)
        } else {
            val ms = parseInput()
            if (ms <= 0) return
            TimerState.start(this, ms)
        }
        AlarmScheduler.scheduleTimer(this, TimerState.endAt(this))
        refresh()
        handler.removeCallbacks(ticker)
        handler.postDelayed(ticker, 250L)
    }

    private fun parseInput(): Long {
        val h = binding.hoursEdit.text.toString().toIntOrNull() ?: 0
        val m = binding.minutesEdit.text.toString().toIntOrNull() ?: 0
        val s = binding.secondsEdit.text.toString().toIntOrNull() ?: 0
        return ((h * 3600L + m * 60L + s) * 1000L)
    }

    private fun pause() {
        if (!TimerState.isRunning(this)) return
        TimerState.pause(this)
        AlarmScheduler.cancelTimer(this)
        refresh()
        handler.removeCallbacks(ticker)
    }

    private fun reset() {
        TimerState.reset(this)
        AlarmScheduler.cancelTimer(this)
        binding.hoursEdit.setText("0")
        binding.minutesEdit.setText("5")
        binding.secondsEdit.setText("0")
        refresh()
        handler.removeCallbacks(ticker)
    }

    private fun refresh() {
        val remaining = TimerState.remainingMs(this)
        binding.remainingText.text = formatMs(remaining)
        val running = TimerState.isRunning(this)
        val hasActive = TimerState.endAt(this) > 0L
        binding.startButton.text = if (running) "Running" else if (remaining > 0) "Resume" else "Start"
        binding.startButton.isClickable = !running
        binding.inputRow.visibility = if (running || hasActive) android.view.View.GONE else android.view.View.VISIBLE
        binding.pauseButton.isEnabled = running
    }

    private fun formatMs(ms: Long): String {
        val totalSec = (ms / 1000).toInt()
        val h = totalSec / 3600
        val m = (totalSec % 3600) / 60
        val s = totalSec % 60
        return String.format(java.util.Locale.US, "%02d:%02d:%02d", h, m, s)
    }
}