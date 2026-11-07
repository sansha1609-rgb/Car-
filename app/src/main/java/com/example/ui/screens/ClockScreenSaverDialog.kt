package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.launcher.TopBarStatus
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin

/**
 * Fullscreen Interactive Screen Saver (triggered by tapping clock):
 * - Prototypes: Digital OLED Minimal, Classic Analog Chronometer, Retro Flip Clock, Ambient Night HUD
 * - Fonts & Formats of date
 * - Dynamic Burn-in Prevention Drift (movement on the screen)
 * - Brightness reduction for dark cabin comfort
 * - In-depth customization modal
 */
@Composable
fun ClockScreenSaverDialog(
    isOpen: Boolean,
    onDismiss: () -> Unit,
    status: TopBarStatus,
    speedKmh: Int
) {
    if (!isOpen) return

    var settings by remember { mutableStateOf(ScreenSaverSettings()) }
    var showSettingsPanel by remember { mutableStateOf(false) }

    // Live clock ticks
    var currentTime by remember { mutableStateOf(Date()) }
    LaunchedEffect(Unit) {
        while (true) {
            currentTime = Date()
            delay(500L)
        }
    }

    // Burn-in prevention drift animation
    val infiniteTransition = rememberInfiniteTransition(label = "burn_in_drift")
    val driftX by infiniteTransition.animateFloat(
        initialValue = -18f,
        targetValue = 18f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 14000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "drift_x"
    )
    val driftY by infiniteTransition.animateFloat(
        initialValue = -12f,
        targetValue = 12f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 18000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "drift_y"
    )

    val actualOffsetX = if (settings.burnInDriftEnabled) driftX.dp else 0.dp
    val actualOffsetY = if (settings.burnInDriftEnabled) driftY.dp else 0.dp

    val timeFormat = remember(settings.showSeconds) {
        if (settings.showSeconds) SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        else SimpleDateFormat("HH:mm", Locale.getDefault())
    }

    val dateFormat = remember(settings.dateFormatPattern) {
        try {
            SimpleDateFormat(settings.dateFormatPattern, Locale.getDefault())
        } catch (_: Exception) {
            SimpleDateFormat("EEEE, d MMMM", Locale.getDefault())
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(if (settings.dimBrightness) Color(0xFF000000) else Color(0xFF050810))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    if (showSettingsPanel) showSettingsPanel = false
                    else onDismiss()
                }
                .testTag("clock_screen_saver_fullscreen")
        ) {
            // Main Clock Stage
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .offset(x = actualOffsetX, y = actualOffsetY),
                contentAlignment = Alignment.Center
            ) {
                when (settings.prototype) {
                    ScreenSaverPrototype.DIGITAL_OLED -> {
                        DigitalOledView(
                            timeStr = timeFormat.format(currentTime),
                            dateStr = dateFormat.format(currentTime),
                            settings = settings,
                            status = status,
                            speedKmh = speedKmh
                        )
                    }
                    ScreenSaverPrototype.ANALOG_CHRONO -> {
                        AnalogChronoView(
                            date = currentTime,
                            settings = settings,
                            status = status,
                            speedKmh = speedKmh
                        )
                    }
                    ScreenSaverPrototype.RETRO_FLIP -> {
                        RetroFlipView(
                            timeStr = timeFormat.format(currentTime),
                            dateStr = dateFormat.format(currentTime),
                            settings = settings,
                            speedKmh = speedKmh
                        )
                    }
                    ScreenSaverPrototype.NIGHT_HUD -> {
                        NightHudView(
                            timeStr = timeFormat.format(currentTime),
                            dateStr = dateFormat.format(currentTime),
                            settings = settings,
                            speedKmh = speedKmh
                        )
                    }
                }
            }

            // Top action buttons (Settings gear & Close)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .align(Alignment.TopEnd),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White.copy(alpha = 0.08f),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
                    modifier = Modifier.clickable { showSettingsPanel = !showSettingsPanel }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Settings, contentDescription = "Customize", tint = settings.clockColor, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("CUSTOMIZE SCREEN SAVER", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                Surface(
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.08f),
                    modifier = Modifier
                        .size(36.dp)
                        .clickable { onDismiss() }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White, modifier = Modifier.size(18.dp))
                    }
                }
            }

            // Bottom Tap Hint
            Text(
                text = "Tap anywhere to wake cockpit",
                color = Color.Gray.copy(alpha = 0.5f),
                fontSize = 11.sp,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 14.dp)
            )

            // Settings Sheet Overlay
            AnimatedVisibility(
                visible = showSettingsPanel,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier.align(Alignment.CenterEnd)
            ) {
                Surface(
                    shape = RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp),
                    color = Color(0xFF101726).copy(alpha = 0.96f),
                    border = BorderStroke(1.dp, settings.clockColor.copy(alpha = 0.4f)),
                    modifier = Modifier
                        .width(360.dp)
                        .fillMaxSize()
                        .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {}
                        .padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("SCREEN SAVER SETTINGS", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Black)
                            IconButton(onClick = { showSettingsPanel = false }, modifier = Modifier.size(28.dp)) {
                                Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                            }
                        }

                        // Prototype Selector
                        Text("Clock Prototype", color = settings.clockColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        ScreenSaverPrototype.values().forEach { proto ->
                            val isSel = proto == settings.prototype
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSel) settings.clockColor.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.05f),
                                border = BorderStroke(1.dp, if (isSel) settings.clockColor else Color.Transparent),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { settings = settings.copy(prototype = proto) }
                            ) {
                                Text(
                                    text = proto.label,
                                    fontSize = 11.sp,
                                    color = if (isSel) Color.White else Color.Gray,
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }

                        // Font Selector
                        Text("Font Family", color = settings.clockColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        ClockFontChoice.values().forEach { font ->
                            val isSel = font == settings.fontChoice
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSel) settings.clockColor.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.05f),
                                border = BorderStroke(1.dp, if (isSel) settings.clockColor else Color.Transparent),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { settings = settings.copy(fontChoice = font) }
                            ) {
                                Text(
                                    text = font.label,
                                    fontFamily = font.fontFamily,
                                    fontSize = 11.sp,
                                    color = if (isSel) Color.White else Color.Gray,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }

                        // Date Format Choices
                        Text("Date Format", color = settings.clockColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        listOf("EEEE, d MMMM", "dd / MM / yyyy", "MM - dd - yyyy", "EEE, d MMM yyyy").forEach { pattern ->
                            val isSel = settings.dateFormatPattern == pattern
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSel) settings.clockColor.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.05f),
                                border = BorderStroke(1.dp, if (isSel) settings.clockColor else Color.Transparent),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { settings = settings.copy(dateFormatPattern = pattern) }
                            ) {
                                Text(
                                    text = pattern,
                                    fontSize = 10.sp,
                                    color = if (isSel) Color.White else Color.Gray,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }

                        // Color Presets
                        Text("Clock Color", color = settings.clockColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(
                                Color(0xFF00E5FF),
                                Color(0xFFFF9100),
                                Color(0xFFFF1744),
                                Color(0xFF00E676),
                                Color(0xFFFFFFFF),
                                Color(0xFFFFD700)
                            ).forEach { c ->
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(c)
                                        .clickable { settings = settings.copy(clockColor = c) }
                                )
                            }
                        }

                        // Clock Size Slider
                        Text("Clock Size: ${settings.clockSizeSp} sp", color = settings.clockColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Slider(
                            value = settings.clockSizeSp.toFloat(),
                            onValueChange = { settings = settings.copy(clockSizeSp = it.toInt()) },
                            valueRange = 54f..114f,
                            colors = SliderDefaults.colors(thumbColor = settings.clockColor, activeTrackColor = settings.clockColor)
                        )

                        // Toggles
                        Text("Elements & Burn-In Protection", color = settings.clockColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)

                        ToggleItem("Show Seconds", settings.showSeconds) { settings = settings.copy(showSeconds = it) }
                        ToggleItem("Show Date", settings.showDate) { settings = settings.copy(showDate = it) }
                        ToggleItem("Show Voltage & Battery", settings.showBatteryVoltage) { settings = settings.copy(showBatteryVoltage = it) }
                        ToggleItem("Show Mini Speedometer", settings.showSpeedMini) { settings = settings.copy(showSpeedMini = it) }
                        ToggleItem("Burn-in Protection Drift", settings.burnInDriftEnabled) { settings = settings.copy(burnInDriftEnabled = it) }
                        ToggleItem("Dim Brightness (Anti-Glare)", settings.dimBrightness) { settings = settings.copy(dimBrightness = it) }
                    }
                }
            }
        }
    }
}

@Composable
private fun ToggleItem(label: String, checked: Boolean, onChecked: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = Color.White, fontSize = 11.sp)
        Switch(
            checked = checked,
            onCheckedChange = onChecked,
            colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF00E5FF))
        )
    }
}

@Composable
private fun DigitalOledView(
    timeStr: String,
    dateStr: String,
    settings: ScreenSaverSettings,
    status: TopBarStatus,
    speedKmh: Int
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = timeStr,
            fontFamily = settings.fontChoice.fontFamily,
            fontSize = settings.clockSizeSp.sp,
            fontWeight = FontWeight.Black,
            color = settings.clockColor,
            letterSpacing = 3.sp
        )

        if (settings.showDate) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = dateStr,
                fontFamily = settings.fontChoice.fontFamily,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White.copy(alpha = 0.85f)
            )
        }

        if (settings.showBatteryVoltage || settings.showSpeedMini) {
            Spacer(modifier = Modifier.height(14.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (settings.showSpeedMini) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color.White.copy(alpha = 0.06f),
                        border = BorderStroke(1.dp, settings.clockColor.copy(alpha = 0.3f))
                    ) {
                        Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Speed, contentDescription = null, tint = settings.clockColor, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("$speedKmh KM/H", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                if (settings.showBatteryVoltage) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color.White.copy(alpha = 0.06f),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f))
                    ) {
                        Text(
                            text = "${status.vehicleVoltage}V • ${status.batteryPercent}%",
                            color = Color.Gray,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AnalogChronoView(
    date: Date,
    settings: ScreenSaverSettings,
    status: TopBarStatus,
    speedKmh: Int
) {
    val cal = remember(date) {
        Calendar.getInstance().apply { time = date }
    }
    val hours = cal.get(Calendar.HOUR)
    val minutes = cal.get(Calendar.MINUTE)
    val seconds = cal.get(Calendar.SECOND)

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Canvas(modifier = Modifier.size(240.dp)) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val radius = size.width / 2f - 12f

            // Outer bezel ring
            drawCircle(
                color = settings.clockColor.copy(alpha = 0.2f),
                radius = radius,
                style = Stroke(width = 6f)
            )

            // Hour ticks
            for (i in 0 until 12) {
                val angle = Math.toRadians((i * 30.0) - 90.0)
                val start = Offset(
                    center.x + (radius - 16f) * cos(angle).toFloat(),
                    center.y + (radius - 16f) * sin(angle).toFloat()
                )
                val end = Offset(
                    center.x + radius * cos(angle).toFloat(),
                    center.y + radius * sin(angle).toFloat()
                )
                drawLine(
                    color = settings.clockColor,
                    start = start,
                    end = end,
                    strokeWidth = 3f,
                    cap = StrokeCap.Round
                )
            }

            // Hour Hand
            val hourAngle = Math.toRadians(((hours + minutes / 60f) * 30.0) - 90.0)
            val hourEnd = Offset(
                center.x + (radius * 0.55f) * cos(hourAngle).toFloat(),
                center.y + (radius * 0.55f) * sin(hourAngle).toFloat()
            )
            drawLine(
                color = Color.White,
                start = center,
                end = hourEnd,
                strokeWidth = 5f,
                cap = StrokeCap.Round
            )

            // Minute Hand
            val minAngle = Math.toRadians((minutes * 6.0) - 90.0)
            val minEnd = Offset(
                center.x + (radius * 0.78f) * cos(minAngle).toFloat(),
                center.y + (radius * 0.78f) * sin(minAngle).toFloat()
            )
            drawLine(
                color = settings.clockColor,
                start = center,
                end = minEnd,
                strokeWidth = 3.5f,
                cap = StrokeCap.Round
            )

            // Second Hand
            if (settings.showSeconds) {
                val secAngle = Math.toRadians((seconds * 6.0) - 90.0)
                val secEnd = Offset(
                    center.x + (radius * 0.88f) * cos(secAngle).toFloat(),
                    center.y + (radius * 0.88f) * sin(secAngle).toFloat()
                )
                drawLine(
                    color = Color(0xFFFF1744),
                    start = center,
                    end = secEnd,
                    strokeWidth = 2f,
                    cap = StrokeCap.Round
                )
            }

            // Pivot Center
            drawCircle(color = Color.White, radius = 6f)
            drawCircle(color = settings.clockColor, radius = 3f)
        }

        if (settings.showDate) {
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = SimpleDateFormat("EEEE, d MMMM", Locale.getDefault()).format(date),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

@Composable
private fun RetroFlipView(
    timeStr: String,
    dateStr: String,
    settings: ScreenSaverSettings,
    speedKmh: Int
) {
    val parts = timeStr.split(":")
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            parts.forEachIndexed { idx, part ->
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFF161F2E),
                    border = BorderStroke(1.5.dp, settings.clockColor.copy(alpha = 0.6f)),
                    modifier = Modifier.size(80.dp, 90.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = part,
                            fontFamily = settings.fontChoice.fontFamily,
                            fontSize = 44.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                    }
                }
                if (idx < parts.size - 1) {
                    Text(":", fontSize = 40.sp, fontWeight = FontWeight.Bold, color = settings.clockColor)
                }
            }
        }

        if (settings.showDate) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(dateStr, color = Color.Gray, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun NightHudView(
    timeStr: String,
    dateStr: String,
    settings: ScreenSaverSettings,
    speedKmh: Int
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = timeStr,
            fontFamily = settings.fontChoice.fontFamily,
            fontSize = 90.sp,
            fontWeight = FontWeight.Black,
            color = settings.clockColor.copy(alpha = 0.85f),
            letterSpacing = 4.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(dateStr, color = Color.Gray.copy(alpha = 0.7f), fontSize = 14.sp)
            Text("•", color = settings.clockColor.copy(alpha = 0.5f))
            Text("$speedKmh KM/H", color = settings.clockColor, fontSize = 14.sp, fontWeight = FontWeight.Black)
        }
    }
}
