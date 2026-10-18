package com.coobyk.mc.clock

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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import com.coobyk.mc.clock.ui.theme.McGreen
import com.coobyk.mc.clock.ui.theme.McTheme
import kotlinx.coroutines.delay

class TimerActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: android.os.Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { McTheme { TimerScreen(this) } }
    }
}

@Composable
private fun TimerScreen(activity: TimerActivity) {
    var remaining by remember { mutableStateOf(TimerState.remainingMs(activity)) }
    var running by remember { mutableStateOf(TimerState.isRunning(activity)) }
    var hasActive by remember { mutableStateOf(TimerState.endAt(activity) > 0L) }
    var h by remember { mutableStateOf("0") }
    var m by remember { mutableStateOf("5") }
    var s by remember { mutableStateOf("0") }

    LaunchedEffect(running) {
        while (running) {
            delay(250)
            remaining = TimerState.remainingMs(activity)
            running = TimerState.isRunning(activity)
            hasActive = TimerState.endAt(activity) > 0L
            if (!running) break
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBg)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("Timer", color = AppText, fontSize = 20.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)

        Text(
            text = formatMs(remaining),
            color = AppText,
            fontSize = 52.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.padding(top = 32.dp),
        )

        if (!running && !hasActive) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 32.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                TimeField(h, { h = it }, "h", Modifier.weight(1f))
                TimeField(m, { m = it }, "m", Modifier.weight(1f))
                TimeField(s, { s = it }, "s", Modifier.weight(1f))
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Button(
                onClick = {
                    if (running) return@Button
                    val rem = TimerState.remainingMs(activity)
                    if (rem > 0 && TimerState.endAt(activity) > 0L) {
                        TimerState.resume(activity)
                    } else {
                        val ms = (h.toIntOrNull() ?: 0) * 3600000L +
                            (m.toIntOrNull() ?: 0) * 60000L +
                            (s.toIntOrNull() ?: 0) * 1000L
                        if (ms <= 0) return@Button
                        TimerState.start(activity, ms)
                    }
                    AlarmScheduler.scheduleTimer(activity, TimerState.endAt(activity))
                    running = true
                    hasActive = true
                    remaining = TimerState.remainingMs(activity)
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = McGreen, contentColor = AppText),
                shape = RoundedCornerShape(8.dp),
                enabled = !running,
            ) {
                Text(if (running) "Running" else if (remaining > 0 && hasActive) "Resume" else "Start", fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, fontSize = 13.sp)
            }
            Button(
                onClick = {
                    if (!running) return@Button
                    TimerState.pause(activity)
                    AlarmScheduler.cancelTimer(activity)
                    running = false
                    remaining = TimerState.remainingMs(activity)
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = AppSurface, contentColor = AppText),
                shape = RoundedCornerShape(8.dp),
                enabled = running,
            ) {
                Text("Pause", fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, fontSize = 13.sp)
            }
            Button(
                onClick = {
                    TimerState.reset(activity)
                    AlarmScheduler.cancelTimer(activity)
                    running = false
                    hasActive = false
                    remaining = 0L
                    h = "0"; m = "5"; s = "0"
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = AppSurface, contentColor = AppText),
                shape = RoundedCornerShape(8.dp),
            ) {
                Text("Reset", fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, fontSize = 13.sp)
            }
        }
    }
}

@Composable
private fun TimeField(value: String, onChange: (String) -> Unit, hint: String, modifier: Modifier) {
    OutlinedTextField(
        value = value,
        onValueChange = { onChange(it.filter { c -> c.isDigit() }.take(2)) },
        modifier = modifier,
        placeholder = { Text(hint, color = AppTextDim) },
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = AppSurface,
            unfocusedContainerColor = AppSurface,
            focusedTextColor = AppText,
            unfocusedTextColor = AppText,
            focusedBorderColor = McGreen,
            unfocusedBorderColor = AppTextDim,
        ),
    )
}

private fun formatMs(ms: Long): String {
    val totalSec = (ms / 1000).toInt()
    val h = totalSec / 3600
    val m = (totalSec % 3600) / 60
    val s = totalSec % 60
    return String.format(java.util.Locale.US, "%02d:%02d:%02d", h, m, s)
}