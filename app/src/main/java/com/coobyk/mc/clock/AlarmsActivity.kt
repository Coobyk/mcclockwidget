package com.coobyk.mc.clock

import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Switch
import android.widget.TextView
import android.widget.TimePicker
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.coobyk.mc.clock.databinding.ActivityAlarmsBinding

class AlarmsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAlarmsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAlarmsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.addAlarmButton.setOnClickListener { showEditDialog(null) }
        binding.exactAlarmHint.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val am = getSystemService(Context.ALARM_SERVICE) as AlarmManager
                if (!am.canScheduleExactAlarms()) {
                    startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM))
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        render()
        updateHint()
    }

    private fun updateHint() {
        val am = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        binding.exactAlarmHint.text = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !am.canScheduleExactAlarms()) {
            "Exact alarms are disabled — tap here to allow them, or alarms won't fire."
        } else {
            ""
        }
    }

    private fun render() {
        val container = binding.alarmsContainer
        container.removeAllViews()
        val alarms = AlarmStore.load(this)
        if (alarms.isEmpty()) {
            val empty = TextView(this).apply {
                text = "No alarms yet. Tap \"Add alarm\"."
                setTextColor(getColor(R.color.app_text_dim))
                textSize = 14f
                setPadding(8, 16, 8, 16)
            }
            container.addView(empty)
            return
        }
        alarms.sortedBy { it.hour * 60 + it.minute }.forEach { alarm ->
            container.addView(makeRow(alarm))
        }
    }

    private fun makeRow(alarm: Alarm): View {
        val panel = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            background = getDrawable(R.drawable.mc_panel)
            setPadding(14, 12, 14, 12)
            val lp = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            lp.bottomMargin = 8
            layoutParams = lp
        }
        val left = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }
        val timeTv = TextView(this).apply {
            text = alarm.timeText()
            setTextColor(getColor(R.color.mc_text))
            textSize = 22f
            typeface = resources.getFont(R.font.silkscreen_bold)
        }
        val subTv = TextView(this).apply {
            text = buildString {
                if (alarm.label.isNotBlank()) append(alarm.label).append("  ·  ")
                append(alarm.daysText())
            }
            setTextColor(getColor(R.color.app_text_dim))
            textSize = 12f
        }
        left.addView(timeTv); left.addView(subTv)

        val toggle = Switch(this).apply {
            isChecked = alarm.enabled
            setOnCheckedChangeListener { _, checked ->
                alarm.enabled = checked
                AlarmStore.save(this@AlarmsActivity, alarm)
                if (checked) AlarmScheduler.schedule(this@AlarmsActivity, alarm) else AlarmScheduler.cancel(this@AlarmsActivity, alarm)
            }
        }
        val deleteBtn = TextView(this).apply {
            text = "✕"
            setTextColor(getColor(R.color.mc_text))
            textSize = 16f
            setPadding(12, 0, 0, 0)
            setOnClickListener {
                AlarmScheduler.cancel(this@AlarmsActivity, alarm)
                AlarmStore.delete(this@AlarmsActivity, alarm.id)
                render()
            }
        }
        panel.addView(left); panel.addView(toggle); panel.addView(deleteBtn)
        panel.setOnClickListener { showEditDialog(alarm) }
        return panel
    }

    private fun showEditDialog(existing: Alarm?) {
        val view = layoutInflater.inflate(R.layout.dialog_alarm_edit, null)
        val tp = view.findViewById<TimePicker>(R.id.timePicker)
        val labelEdit = view.findViewById<EditText>(R.id.labelEdit)
        val dayChecks = (0..6).map { view.findViewById<CheckBox>(resources.getIdentifier("d$it", "id", packageName)) }
        tp.setIs24HourView(true)
        val alarm = existing ?: Alarm(id = System.currentTimeMillis())
        tp.hour = alarm.hour; tp.minute = alarm.minute
        labelEdit.setText(alarm.label)
        for (i in 0..6) dayChecks[i].isChecked = (alarm.days and (1 shl i)) != 0

        AlertDialog.Builder(this)
            .setTitle(if (existing == null) "New alarm" else "Edit alarm")
            .setView(view)
            .setPositiveButton("Save") { _, _ ->
                alarm.hour = tp.hour
                alarm.minute = tp.minute
                alarm.label = labelEdit.text.toString().trim()
                var d = 0
                for (i in 0..6) if (dayChecks[i].isChecked) d = d or (1 shl i)
                alarm.days = d
                if (existing == null) alarm.id = System.currentTimeMillis()
                AlarmStore.save(this, alarm)
                if (alarm.enabled) AlarmScheduler.schedule(this, alarm)
                render()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}