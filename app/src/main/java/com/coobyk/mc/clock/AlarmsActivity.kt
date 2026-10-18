package com.coobyk.mc.clock

import android.app.TimePickerDialog
import android.content.Context
import android.os.Bundle
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.widget.Toast
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.coobyk.mc.clock.ui.theme.AppBg
import com.coobyk.mc.clock.ui.theme.AppSurface
import com.coobyk.mc.clock.ui.theme.AppText
import com.coobyk.mc.clock.ui.theme.AppTextDim
import com.coobyk.mc.clock.ui.theme.AppTextSecondary
import com.coobyk.mc.clock.ui.theme.McGreen
import com.coobyk.mc.clock.ui.theme.McTheme

class AlarmsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            McTheme {
                AlarmsScreen(this)
            }
        }
    }
}

@Composable
private fun AlarmsScreen(activity: AlarmsActivity) {
    val alarms = remember { mutableStateListOf<Alarm>() }
    var showEdit by remember { mutableStateOf<Alarm?>(null) } // null = hidden; a sentinel for "new"
    var editingNew by remember { mutableStateOf(false) }
    var exactHint by remember { mutableStateOf("") }

    fun reload() {
        alarms.clear()
        alarms.addAll(AlarmStore.load(activity))
    }

    // initial load
    androidx.compose.runtime.LaunchedEffect(Unit) {
        reload()
        updateHint(activity) { exactHint = it }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBg)
            .padding(20.dp),
    ) {
        Text(
            text = "Alarms",
            color = AppText,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
        )

        if (exactHint.isNotEmpty()) {
            Text(
                text = exactHint,
                color = AppTextDim,
                fontSize = 11.sp,
                modifier = Modifier.padding(top = 8.dp),
            )
        }

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(top = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            if (alarms.isEmpty()) {
                item {
                    Text(
                        text = "No alarms yet. Tap \"Add alarm\".",
                        color = AppTextDim,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(8.dp, 16.dp),
                    )
                }
            }
            items(alarms.sortedBy { it.hour * 60 + it.minute }, key = { it.id }) { alarm ->
                AlarmRow(
                    alarm = alarm,
                    onToggle = { checked ->
                        alarm.enabled = checked
                        AlarmStore.save(activity, alarm)
                        if (checked) AlarmScheduler.schedule(activity, alarm) else AlarmScheduler.cancel(activity, alarm)
                        reload()
                    },
                    onDelete = {
                        AlarmScheduler.cancel(activity, alarm)
                        AlarmStore.delete(activity, alarm.id)
                        reload()
                    },
                    onClick = { showEdit = alarm; editingNew = false },
                )
            }
        }

        TextButton(
            onClick = { showEdit = Alarm(id = System.currentTimeMillis()); editingNew = true },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
        ) {
            Text(
                text = "Add alarm",
                color = AppText,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                fontSize = 13.sp,
            )
        }
    }

    val current = showEdit
    if (current != null) {
        AlarmEditDialog(
            activity = activity,
            alarm = current,
            isNew = editingNew,
            onDismiss = { showEdit = null },
            onSaved = {
                showEdit = null
                reload()
            },
        )
    }
}

@Composable
private fun AlarmRow(
    alarm: Alarm,
    onToggle: (Boolean) -> Unit,
    onDelete: () -> Unit,
    onClick: () -> Unit,
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = AppSurface),
        shape = RoundedCornerShape(10.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = alarm.timeText(),
                    color = AppText,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                )
                Text(
                    text = buildString {
                        if (alarm.label.isNotBlank()) append(alarm.label).append("  ·  ")
                        append(alarm.daysText())
                    },
                    color = AppTextDim,
                    fontSize = 12.sp,
                )
            }
            Switch(
                checked = alarm.enabled,
                onCheckedChange = onToggle,
                colors = SwitchDefaults.colors(checkedThumbColor = McGreen, checkedTrackColor = McGreen.copy(alpha = 0.5f)),
            )
            TextButton(onClick = onDelete) {
                Text(text = "✕", color = AppText, fontSize = 16.sp)
            }
        }
    }
}

@Composable
private fun AlarmEditDialog(
    activity: AlarmsActivity,
    alarm: Alarm,
    isNew: Boolean,
    onDismiss: () -> Unit,
    onSaved: () -> Unit,
) {
    var hour by remember { mutableStateOf(alarm.hour) }
    var minute by remember { mutableStateOf(alarm.minute) }
    var label by remember { mutableStateOf(alarm.label) }
    val days = remember { mutableStateListOf<Boolean>().apply { repeat(7) { add((alarm.days and (1 shl it)) != 0) } } }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isNew) "New alarm" else "Edit alarm", color = AppText) },
        text = {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TextButton(onClick = {
                        TimePickerDialog(activity, { _, h, m -> hour = h; minute = m }, hour, minute, true).show()
                    }) {
                        Text(
                            text = String.format(java.util.Locale.US, "%02d:%02d", hour, minute),
                            color = McGreen,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                        )
                    }
                }
                TextField(
                    value = label,
                    onValueChange = { label = it },
                    placeholder = { Text("Label (optional)", color = AppTextDim) },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = AppSurface,
                        unfocusedContainerColor = AppSurface,
                        focusedTextColor = AppText,
                        unfocusedTextColor = AppText,
                    ),
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                )
                Text(text = "Repeat", color = AppTextSecondary, fontSize = 12.sp, modifier = Modifier.padding(top = 12.dp, bottom = 4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    val names = arrayOf("Su", "Mo", "Tu", "We", "Th", "Fr", "Sa")
                    for (i in 0..3) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Checkbox(
                                checked = days[i],
                                onCheckedChange = { days[i] = it },
                                colors = CheckboxDefaults.colors(checkedColor = McGreen),
                            )
                            Text(names[i], color = AppTextDim, fontSize = 10.sp)
                        }
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    val names = arrayOf("Su", "Mo", "Tu", "We", "Th", "Fr", "Sa")
                    for (i in 4..6) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Checkbox(
                                checked = days[i],
                                onCheckedChange = { days[i] = it },
                                colors = CheckboxDefaults.colors(checkedColor = McGreen),
                            )
                            Text(names[i], color = AppTextDim, fontSize = 10.sp)
                        }
                    }
                }
                Text(
                    text = "Leave all unchecked for a one-time alarm.",
                    color = AppTextDim,
                    fontSize = 10.sp,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                alarm.hour = hour
                alarm.minute = minute
                alarm.label = label.trim()
                var d = 0
                for (i in 0..6) if (days[i]) d = d or (1 shl i)
                alarm.days = d
                if (isNew) alarm.id = System.currentTimeMillis()
                AlarmStore.save(activity, alarm)
                if (alarm.enabled) AlarmScheduler.schedule(activity, alarm)
                onSaved()
            }) { Text("Save", color = McGreen) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", color = AppTextSecondary) } },
        containerColor = AppSurface,
    )
}

private fun updateHint(activity: AlarmsActivity, set: (String) -> Unit) {
    val am = activity.getSystemService(Context.ALARM_SERVICE) as android.app.AlarmManager
    set(
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !am.canScheduleExactAlarms()) {
            "Exact alarms are disabled — tap here to allow them, or alarms won't fire."
        } else ""
    )
}