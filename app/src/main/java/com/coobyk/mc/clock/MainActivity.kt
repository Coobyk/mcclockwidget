package com.coobyk.mc.clock

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.viewinterop.AndroidView
import com.coobyk.mc.clock.widget.ClockWidgetProvider
import com.coobyk.mc.clock.ui.theme.AppBg
import com.coobyk.mc.clock.ui.theme.AppBorder
import com.coobyk.mc.clock.ui.theme.AppSurface
import com.coobyk.mc.clock.ui.theme.AppText
import com.coobyk.mc.clock.ui.theme.AppTextDim
import com.coobyk.mc.clock.ui.theme.AppTextSecondary
import com.coobyk.mc.clock.ui.theme.McGreen
import com.coobyk.mc.clock.ui.theme.McTheme
import java.time.LocalDateTime

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        NotificationHelper.ensureChannels(this)
        requestPerms()
        setContent {
            McTheme {
                MainScreen(
                    onAlarms = { startActivity(Intent(this, AlarmsActivity::class.java)) },
                    onTimer = { startActivity(Intent(this, TimerActivity::class.java)) },
                    onStopwatch = { startActivity(Intent(this, StopwatchActivity::class.java)) },
                    onAddWidget = { pinWidget() },
                )
            }
        }
    }

    private fun requestPerms() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 1001)
            }
        }
    }

    private fun pinWidget() {
        val manager = android.appwidget.AppWidgetManager.getInstance(this)
        val provider = android.content.ComponentName(this, ClockWidgetProvider::class.java)
        if (manager == null || !manager.isRequestPinAppWidgetSupported) {
            android.widget.Toast.makeText(this, R.string.pin_unsupported, android.widget.Toast.LENGTH_LONG).show()
            return
        }
        if (!manager.requestPinAppWidget(provider, null, null)) {
            android.widget.Toast.makeText(this, R.string.pin_unsupported, android.widget.Toast.LENGTH_LONG).show()
        }
    }
}

@Composable
private fun MainScreen(
    onAlarms: () -> Unit,
    onTimer: () -> Unit,
    onStopwatch: () -> Unit,
    onAddWidget: () -> Unit,
) {
    val scroll = rememberScrollState()
    var now by remember { mutableStateOf(LocalDateTime.now()) }
    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(1000)
            now = LocalDateTime.now()
        }
    }
    val dayTime = MinecraftTime.dayTimeFor(now)
    val twentyFour = android.text.format.DateFormat.is24HourFormat(androidx.compose.ui.platform.LocalContext.current)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBg)
            .verticalScroll(scroll)
            .padding(20.dp),
    ) {
        Text(
            text = androidx.compose.ui.res.stringResource(R.string.app_name),
            color = AppText,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
        )
        Text(
            text = androidx.compose.ui.res.stringResource(R.string.app_tagline),
            color = AppTextDim,
            fontSize = 13.sp,
            modifier = Modifier.padding(top = 4.dp),
        )

        // Hero dial
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 20.dp),
            colors = CardDefaults.cardColors(containerColor = AppSurface),
            shape = RoundedCornerShape(10.dp),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                contentAlignment = Alignment.Center,
            ) {
                AndroidView(
                    factory = { ctx ->
                        ClockView(ctx).apply { refresh() }
                    },
                    update = { view -> view.refresh() },
                    modifier = Modifier.size(220.dp),
                )
            }
        }

        Text(
            text = digits(now, twentyFour, true),
            color = AppText,
            fontSize = 40.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(top = 16.dp),
        )

        // Nav
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            NavButton("Alarms", McGreen, Modifier.weight(1f), onAlarms)
            NavButton("Timer", McGreen, Modifier.weight(1f), onTimer)
            NavButton("Stopwatch", McGreen, Modifier.weight(1f), onStopwatch)
        }

        // Info panel
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 20.dp),
            colors = CardDefaults.cardColors(containerColor = AppSurface),
            shape = RoundedCornerShape(10.dp),
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                InfoRow(androidx.compose.ui.res.stringResource(R.string.label_local), digits(now, twentyFour, false))
                InfoRow(androidx.compose.ui.res.stringResource(R.string.label_minecraft), MinecraftTime.worldClockText(dayTime))
                InfoRow(androidx.compose.ui.res.stringResource(R.string.label_ticks), "${dayTime.toInt()} ticks")
                InfoRow(androidx.compose.ui.res.stringResource(R.string.label_frame), "${MinecraftTime.clockFrame(dayTime)} / 63")
                InfoRow(androidx.compose.ui.res.stringResource(R.string.label_phase), MinecraftTime.phase(dayTime).label)
            }
        }

        Button(
            onClick = onAddWidget,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 20.dp),
            colors = ButtonDefaults.buttonColors(containerColor = McGreen, contentColor = AppText),
            shape = RoundedCornerShape(8.dp),
        ) {
            Text(
                text = androidx.compose.ui.res.stringResource(R.string.add_widget),
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                fontSize = 13.sp,
            )
        }

        Text(
            text = androidx.compose.ui.res.stringResource(R.string.about),
            color = AppTextDim,
            fontSize = 12.sp,
            modifier = Modifier.padding(top = 20.dp, bottom = 8.dp),
        )
    }
}

@Composable
private fun NavButton(label: String, color: Color, modifier: Modifier, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(containerColor = AppSurface, contentColor = color),
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, AppBorder),
    ) {
        Text(
            text = label,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            fontSize = 12.sp,
        )
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(text = label, color = AppTextSecondary, fontSize = 13.sp)
        Text(text = value, color = AppText, fontSize = 13.sp, fontWeight = FontWeight.Medium, fontFamily = FontFamily.Monospace)
    }
}

private fun digits(now: LocalDateTime, twentyFourHour: Boolean, withSeconds: Boolean): String {
    val seconds = if (withSeconds) String.format(java.util.Locale.US, ":%02d", now.second) else ""
    return if (twentyFourHour) {
        String.format(java.util.Locale.US, "%02d:%02d%s", now.hour, now.minute, seconds)
    } else {
        val hour = now.hour % 12
        String.format(
            java.util.Locale.US,
            "%d:%02d%s %s",
            if (hour == 0) 12 else hour,
            now.minute,
            seconds,
            if (now.hour < 12) "AM" else "PM",
        )
    }
}