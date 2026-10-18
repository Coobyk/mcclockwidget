package com.coobyk.mc.clock

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.coobyk.mc.clock.databinding.ActivityStopwatchBinding

class StopwatchActivity : AppCompatActivity() {
    private lateinit var binding: ActivityStopwatchBinding
    private var running = false
    private var elapsedMs = 0L
    private var startAt = 0L
    private var lapCount = 0
    private val handler = Handler(Looper.getMainLooper())
    private val ticker = object : Runnable {
        override fun run() {
            refresh()
            if (running) handler.postDelayed(this, 33L)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStopwatchBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.startStopButton.setOnClickListener { toggle() }
        binding.lapButton.setOnClickListener { lap() }
        binding.resetButton.setOnClickListener { reset() }
        refresh()
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(ticker)
        if (running) elapsedMs += System.currentTimeMillis() - startAt; startAt = System.currentTimeMillis()
    }

    override fun onResume() {
        super.onResume()
        if (running) { startAt = System.currentTimeMillis(); handler.postDelayed(ticker, 33L) }
        refresh()
    }

    private fun toggle() {
        if (running) {
            elapsedMs += System.currentTimeMillis() - startAt
            running = false
            handler.removeCallbacks(ticker)
        } else {
            startAt = System.currentTimeMillis()
            running = true
            handler.postDelayed(ticker, 33L)
        }
        refresh()
    }

    private fun lap() {
        if (!running && elapsedMs == 0L) return
        lapCount++
        val ms = currentElapsed()
        val row = TextView(this).apply {
            text = String.format(java.util.Locale.US, "Lap %02d   %s", lapCount, formatMs(ms))
            setTextColor(getColor(R.color.app_text))
            textSize = 14f
            typeface = resources.getFont(R.font.silkscreen_bold)
            setPadding(8, 6, 8, 6)
        }
        binding.lapsContainer.addView(row, 0)
    }

    private fun reset() {
        running = false
        elapsedMs = 0L
        lapCount = 0
        handler.removeCallbacks(ticker)
        binding.lapsContainer.removeAllViews()
        refresh()
    }

    private fun currentElapsed(): Long =
        if (running) elapsedMs + (System.currentTimeMillis() - startAt) else elapsedMs

    private fun refresh() {
        binding.timeText.text = formatMs(currentElapsed())
        binding.startStopButton.text = if (running) "Stop" else "Start"
    }

    private fun formatMs(ms: Long): String {
        val totalSec = ms / 1000.0
        val min = (totalSec / 60).toInt()
        val sec = (totalSec % 60)
        return String.format(java.util.Locale.US, "%02d:%05.2f", min, sec)
    }
}