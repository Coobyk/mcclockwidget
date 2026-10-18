package com.coobyk.mc.clock

import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.coobyk.mc.clock.ui.theme.McGreen
import com.coobyk.mc.clock.ui.theme.McTheme
import kotlinx.coroutines.delay

class StopwatchActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: android.os.Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { McTheme { StopwatchScreen() } }
    }
}

@Composable
private fun StopwatchScreen() {
    var running by remember { mutableStateOf(false) }
    var elapsedMs by remember { mutableStateOf(0L) }
    var startAt by remember { mutableStateOf(0L) }
    var now by remember { mutableStateOf(System.currentTimeMillis()) }
    val laps = remember { mutableStateListOf<Long>() }
    var lapCount by remember { mutableStateOf(0) }

    fun currentElapsed(): Long = if (running) elapsedMs + (now - startAt) else elapsedMs

    LaunchedEffect(running) {
        while (running) {
            delay(33)
            now = System.currentTimeMillis()
        }
    }

    // (removed redundant tick loop)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBg)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("Stopwatch", color = AppText, fontSize = 20.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)

        Text(
            text = formatSw(currentElapsed()),
            color = AppText,
            fontSize = 52.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.padding(top = 32.dp),
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Button(
                onClick = {
                    if (running) {
                        elapsedMs += System.currentTimeMillis() - startAt
                        running = false
                    } else {
                        startAt = System.currentTimeMillis()
                        running = true
                    }
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = McGreen, contentColor = AppText),
                shape = RoundedCornerShape(8.dp),
            ) {
                Text(if (running) "Stop" else "Start", fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, fontSize = 13.sp)
            }
            Button(
                onClick = {
                    if (!running && elapsedMs == 0L) return@Button
                    lapCount++
                    laps.add(0, currentElapsed())
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = AppSurface, contentColor = AppText),
                shape = RoundedCornerShape(8.dp),
            ) {
                Text("Lap", fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, fontSize = 13.sp)
            }
            Button(
                onClick = {
                    running = false
                    elapsedMs = 0L
                    lapCount = 0
                    laps.clear()
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = AppSurface, contentColor = AppText),
                shape = RoundedCornerShape(8.dp),
            ) {
                Text("Reset", fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, fontSize = 13.sp)
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
                .weight(1f),
        ) {
            items(laps) { ms ->
                Text(
                    text = formatSw(ms),
                    color = AppText,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(vertical = 6.dp),
                )
            }
        }
    }
}

private fun formatSw(ms: Long): String {
    val totalSec = ms / 1000.0
    val min = (totalSec / 60).toInt()
    val sec = totalSec % 60
    return String.format(java.util.Locale.US, "%02d:%05.2f", min, sec)
}