package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AvTimer
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.launcher.AddressInfo
import com.example.launcher.WeatherInfo
import com.example.ui.theme.SpeedAlertRed
import kotlinx.coroutines.delay
import java.util.Calendar
import java.util.Date
import kotlin.math.cos
import kotlin.math.sin

/**
 * Audio Spectrum EQ Visualizer Widget:
 * Animated jumping frequency bars reacting to vehicle music playback.
 */
@Composable
fun AudioSpectrumVisualizerWidget(
    isPlaying: Boolean,
    tint: Color = MaterialTheme.colorScheme.primary,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "audio_eq")
    val anim1 by infiniteTransition.animateFloat(
        initialValue = 0.2f, targetValue = 0.95f,
        animationSpec = infiniteRepeatable(tween(240, easing = LinearEasing), RepeatMode.Reverse),
        label = "eq1"
    )
    val anim2 by infiniteTransition.animateFloat(
        initialValue = 0.4f, targetValue = 0.85f,
        animationSpec = infiniteRepeatable(tween(310, easing = LinearEasing), RepeatMode.Reverse),
        label = "eq2"
    )
    val anim3 by infiniteTransition.animateFloat(
        initialValue = 0.15f, targetValue = 1.0f,
        animationSpec = infiniteRepeatable(tween(190, easing = LinearEasing), RepeatMode.Reverse),
        label = "eq3"
    )
    val anim4 by infiniteTransition.animateFloat(
        initialValue = 0.3f, targetValue = 0.75f,
        animationSpec = infiniteRepeatable(tween(270, easing = LinearEasing), RepeatMode.Reverse),
        label = "eq4"
    )
    val anim5 by infiniteTransition.animateFloat(
        initialValue = 0.5f, targetValue = 0.9f,
        animationSpec = infiniteRepeatable(tween(360, easing = LinearEasing), RepeatMode.Reverse),
        label = "eq5"
    )

    val factors = listOf(anim1, anim2, anim3, anim4, anim5, anim2, anim4, anim1, anim3)

    Row(
        modifier = modifier
            .height(28.dp),
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        factors.forEach { factor ->
            val heightPercent = if (isPlaying) factor else 0.15f
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight(heightPercent)
                    .clip(RoundedCornerShape(2.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(tint, tint.copy(alpha = 0.4f))
                        )
                    )
            )
        }
    }
}

/**
 * Analog Clock Face Widget with luxury automotive dial markings
 */
@Composable
fun AnalogClockWidget(
    tint: Color = MaterialTheme.colorScheme.primary,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    var currentTime by remember { mutableStateOf(Date()) }
    LaunchedEffect(Unit) {
        while (true) {
            currentTime = Date()
            delay(1000L)
        }
    }

    val cal = remember(currentTime) { Calendar.getInstance().apply { time = currentTime } }
    val hours = cal.get(Calendar.HOUR)
    val minutes = cal.get(Calendar.MINUTE)
    val seconds = cal.get(Calendar.SECOND)

    Box(
        modifier = modifier
            .clip(CircleShape)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val radius = size.width / 2f - 4f

            // Outer dial ring
            drawCircle(
                color = tint.copy(alpha = 0.3f),
                radius = radius,
                style = Stroke(width = 3f)
            )

            // Hour marks
            for (i in 0 until 12) {
                val angle = Math.toRadians((i * 30.0) - 90.0)
                val start = Offset(
                    center.x + (radius - 8f) * cos(angle).toFloat(),
                    center.y + (radius - 8f) * sin(angle).toFloat()
                )
                val end = Offset(
                    center.x + radius * cos(angle).toFloat(),
                    center.y + radius * sin(angle).toFloat()
                )
                drawLine(
                    color = if (i % 3 == 0) tint else Color.Gray,
                    start = start,
                    end = end,
                    strokeWidth = if (i % 3 == 0) 2.5f else 1.5f
                )
            }

            // Hour Hand
            val hourAngle = Math.toRadians(((hours + minutes / 60f) * 30.0) - 90.0)
            drawLine(
                color = Color.White,
                start = center,
                end = Offset(
                    center.x + (radius * 0.5f) * cos(hourAngle).toFloat(),
                    center.y + (radius * 0.5f) * sin(hourAngle).toFloat()
                ),
                strokeWidth = 3.5f,
                cap = StrokeCap.Round
            )

            // Minute Hand
            val minAngle = Math.toRadians((minutes * 6.0) - 90.0)
            drawLine(
                color = tint,
                start = center,
                end = Offset(
                    center.x + (radius * 0.72f) * cos(minAngle).toFloat(),
                    center.y + (radius * 0.72f) * sin(minAngle).toFloat()
                ),
                strokeWidth = 2.5f,
                cap = StrokeCap.Round
            )

            // Second Hand
            val secAngle = Math.toRadians((seconds * 6.0) - 90.0)
            drawLine(
                color = Color(0xFFFF1744),
                start = center,
                end = Offset(
                    center.x + (radius * 0.85f) * cos(secAngle).toFloat(),
                    center.y + (radius * 0.85f) * sin(secAngle).toFloat()
                ),
                strokeWidth = 1.5f,
                cap = StrokeCap.Round
            )

            // Pivot Center
            drawCircle(color = Color.White, radius = 3.5f)
        }
    }
}

/**
 * Analog Needle Speedometer Gauge Widget
 */
@Composable
fun AnalogSpeedometerWidget(
    speed: Float,
    maxSpeed: Float = 180f,
    tint: Color = MaterialTheme.colorScheme.primary,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val radius = size.width / 2f - 8f

        // Background Arc (135° to 405° = 270° sweep)
        drawArc(
            color = Color.White.copy(alpha = 0.12f),
            startAngle = 135f,
            sweepAngle = 270f,
            useCenter = false,
            style = Stroke(width = 8f, cap = StrokeCap.Round)
        )

        // Active Arc
        val currentFraction = (speed / maxSpeed).coerceIn(0f, 1f)
        val activeSweep = currentFraction * 270f
        drawArc(
            brush = Brush.sweepGradient(
                listOf(tint, Color(0xFF00E676), SpeedAlertRed)
            ),
            startAngle = 135f,
            sweepAngle = activeSweep,
            useCenter = false,
            style = Stroke(width = 8f, cap = StrokeCap.Round)
        )

        // Needle
        val needleAngleRad = Math.toRadians((135.0 + activeSweep))
        val needleEnd = Offset(
            center.x + (radius * 0.82f) * cos(needleAngleRad).toFloat(),
            center.y + (radius * 0.82f) * sin(needleAngleRad).toFloat()
        )
        drawLine(
            color = Color(0xFFFF1744),
            start = center,
            end = needleEnd,
            strokeWidth = 4f,
            cap = StrokeCap.Round
        )

        // Pivot Hub
        drawCircle(color = Color.White, radius = 6f)
        drawCircle(color = Color.Black, radius = 3f)
    }
}

/**
 * Address & Real-time Location Widget
 */
@Composable
fun AddressLocationWidget(
    address: AddressInfo,
    tint: Color = MaterialTheme.colorScheme.primary,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = modifier.height(30.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.LocationOn, contentDescription = null, tint = tint, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "${address.street}, ${address.city}",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 1,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Surface(
                shape = RoundedCornerShape(4.dp),
                color = Color.White.copy(alpha = 0.08f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Explore, contentDescription = null, tint = tint, modifier = Modifier.size(10.dp))
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(address.compassDirection, fontSize = 9.sp, fontWeight = FontWeight.Black, color = Color.White)
                }
            }
        }
    }
}

/**
 * Weather Telemetry Widget
 */
@Composable
fun WeatherWidget(
    weather: WeatherInfo,
    tint: Color = MaterialTheme.colorScheme.primary,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = modifier.height(30.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (weather.condition.contains("Rain", true)) Icons.Default.Cloud else Icons.Default.WbSunny,
                    contentDescription = null,
                    tint = Color(0xFFFFB300),
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text("${weather.temperatureC}°C", fontSize = 11.sp, fontWeight = FontWeight.Black, color = Color.White)
                Spacer(modifier = Modifier.width(4.dp))
                Text(weather.condition, fontSize = 9.sp, color = Color.Gray)
            }

            Text("Wind ${weather.windSpeedKmh}k • ${weather.humidityPercent}%", fontSize = 9.sp, color = Color.LightGray)
        }
    }
}
