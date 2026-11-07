package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.SpeedAlertRed
import com.example.ui.theme.SpeedSafeGreen
import kotlin.math.cos
import kotlin.math.sin

/**
 * Large, premium automotive digital instrument cluster speedometer:
 * - Oversized 260dp-320dp multi-tier circular cluster
 * - Outer RPM / Tachometer arc (0-7000 RPM with redline)
 * - Inner high-contrast Speedometer arc (0-200 km/h) with sweeping neon glow
 * - GIANT 70sp digital speed readout in monospace font
 * - Speed limit indicator badge with overspeed alert
 * - Gear indicator ("D", "P", "R", "N") & live telemetry badges
 */
@Composable
fun LargeSpeedometerCluster(
    currentSpeedKmh: Int,
    speedLimitKmh: Int,
    isOverSpeed: Boolean,
    engineRpm: Int = 2100,
    gear: String = "D",
    isKmh: Boolean = true,
    compact: Boolean = true,
    onAdjustSpeed: (Int) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val animatedSpeed by animateFloatAsState(
        targetValue = currentSpeedKmh.toFloat(),
        animationSpec = tween(durationMillis = 350),
        label = "large_speed"
    )

    val animatedRpm by animateFloatAsState(
        targetValue = engineRpm.toFloat(),
        animationSpec = tween(durationMillis = 350),
        label = "large_rpm"
    )

    val gaugeColor by animateColorAsState(
        targetValue = if (isOverSpeed) SpeedAlertRed else MaterialTheme.colorScheme.primary,
        animationSpec = tween(250),
        label = "large_gauge_color"
    )

    val secondaryColor = MaterialTheme.colorScheme.secondary
    val primaryColor = MaterialTheme.colorScheme.primary
    val maxGaugeSpeed = if (isKmh) 200f else 130f
    val speedUnitLabel = if (isKmh) "KM/H" else "MPH"

    val clusterSize = if (compact) 170.dp else 260.dp

    Column(
        modifier = modifier.testTag("large_speedometer_cluster"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Main Circular Cluster
        Box(
            modifier = Modifier
                .size(clusterSize)
                .padding(2.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = Offset(size.width / 2, size.height / 2)
                val strokeOuterRpm = if (compact) 3.5.dp.toPx() else 6.dp.toPx()
                val strokeMainSpeed = if (compact) 9.dp.toPx() else 15.dp.toPx()

                val radiusRpm = (size.minDimension / 2) - strokeOuterRpm - 2.dp.toPx()
                val radiusSpeed = radiusRpm - strokeMainSpeed - (if (compact) 3.dp.toPx() else 6.dp.toPx())

                val startAngle = 135f
                val sweepAngle = 270f

                // 1. OUTER TACHOMETER (RPM) BACKGROUND ARC
                drawArc(
                    color = Color.DarkGray.copy(alpha = 0.35f),
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    topLeft = Offset(center.x - radiusRpm, center.y - radiusRpm),
                    size = Size(radiusRpm * 2, radiusRpm * 2),
                    style = Stroke(width = strokeOuterRpm, cap = StrokeCap.Round)
                )

                // 1.1 OUTER TACHOMETER ACTIVE ARC (0-7000 RPM)
                val rpmFraction = (animatedRpm / 7000f).coerceIn(0f, 1f)
                val currentRpmSweep = sweepAngle * rpmFraction
                val isRedline = animatedRpm >= 5800
                drawArc(
                    brush = Brush.sweepGradient(
                        0.0f to primaryColor.copy(alpha = 0.5f),
                        0.75f to if (isRedline) SpeedAlertRed else primaryColor,
                        1.0f to if (isRedline) SpeedAlertRed else secondaryColor
                    ),
                    startAngle = startAngle,
                    sweepAngle = currentRpmSweep,
                    useCenter = false,
                    topLeft = Offset(center.x - radiusRpm, center.y - radiusRpm),
                    size = Size(radiusRpm * 2, radiusRpm * 2),
                    style = Stroke(width = strokeOuterRpm, cap = StrokeCap.Round)
                )

                // 2. INNER SPEEDOMETER BACKGROUND ARC
                drawArc(
                    color = Color(0xFF161F30),
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    topLeft = Offset(center.x - radiusSpeed, center.y - radiusSpeed),
                    size = Size(radiusSpeed * 2, radiusSpeed * 2),
                    style = Stroke(width = strokeMainSpeed, cap = StrokeCap.Round)
                )

                // 2.1 INNER SPEEDOMETER ACTIVE ARC
                val speedFraction = (animatedSpeed / maxGaugeSpeed).coerceIn(0f, 1f)
                val currentSpeedSweep = sweepAngle * speedFraction
                drawArc(
                    brush = Brush.sweepGradient(
                        0.0f to secondaryColor,
                        0.6f to gaugeColor,
                        1.0f to if (isOverSpeed) SpeedAlertRed else gaugeColor
                    ),
                    startAngle = startAngle,
                    sweepAngle = currentSpeedSweep,
                    useCenter = false,
                    topLeft = Offset(center.x - radiusSpeed, center.y - radiusSpeed),
                    size = Size(radiusSpeed * 2, radiusSpeed * 2),
                    style = Stroke(width = strokeMainSpeed, cap = StrokeCap.Round)
                )

                // 2.2 SPEED TICK MARKS
                val numTicks = 10
                for (i in 0..numTicks) {
                    val tickAngle = startAngle + (sweepAngle / numTicks) * i
                    val rad = Math.toRadians(tickAngle.toDouble())
                    val tickInnerRadius = radiusSpeed - 12.dp.toPx()
                    val tickOuterRadius = radiusSpeed - 4.dp.toPx()

                    val startX = center.x + tickInnerRadius * cos(rad).toFloat()
                    val startY = center.y + tickInnerRadius * sin(rad).toFloat()
                    val endX = center.x + tickOuterRadius * cos(rad).toFloat()
                    val endY = center.y + tickOuterRadius * sin(rad).toFloat()

                    drawLine(
                        color = Color.LightGray.copy(alpha = 0.5f),
                        start = Offset(startX, startY),
                        end = Offset(endX, endY),
                        strokeWidth = 2.dp.toPx()
                    )
                }

                // 3. GLOWING NEEDLE TIP
                val needleAngleRad = Math.toRadians((startAngle + currentSpeedSweep).toDouble())
                val needleX = center.x + radiusSpeed * cos(needleAngleRad).toFloat()
                val needleY = center.y + radiusSpeed * sin(needleAngleRad).toFloat()

                // Outer halo
                drawCircle(
                    color = gaugeColor.copy(alpha = 0.4f),
                    radius = 14.dp.toPx(),
                    center = Offset(needleX, needleY)
                )
                // White center core
                drawCircle(
                    color = Color.White,
                    radius = 7.dp.toPx(),
                    center = Offset(needleX, needleY)
                )
                drawCircle(
                    color = gaugeColor,
                    radius = 4.dp.toPx(),
                    center = Offset(needleX, needleY)
                )
            }

            // CENTER INFORMATION DISPLAY
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Gear Indicator Bar: P R N [D] S
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    listOf("P", "R", "N", "D", "S").forEach { g ->
                        val isCurrentGear = g == gear
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = if (isCurrentGear) MaterialTheme.colorScheme.primary else Color.Transparent,
                            border = BorderStroke(
                                1.dp,
                                if (isCurrentGear) MaterialTheme.colorScheme.primary else Color.DarkGray
                            ),
                            modifier = Modifier.padding(1.dp)
                        ) {
                            Text(
                                text = g,
                                fontSize = 10.sp,
                                fontWeight = if (isCurrentGear) FontWeight.Black else FontWeight.Bold,
                                color = if (isCurrentGear) Color.Black else Color.Gray,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                // SPEED VALUE
                Text(
                    text = "$currentSpeedKmh",
                    fontSize = if (compact) 42.sp else 70.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = (-1).sp,
                    color = if (isOverSpeed) SpeedAlertRed else MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.testTag("digital_speed_text")
                )

                // Speed Unit (KM/H)
                Text(
                    text = speedUnitLabel,
                    fontSize = if (compact) 10.sp else 13.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(if (compact) 2.dp else 4.dp))

                // Bottom badge: Speed Limit Circle Sign & RPM Readout
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Speed Limit Sign (Red ring circle)
                    Surface(
                        shape = CircleShape,
                        color = Color.White,
                        border = BorderStroke(2.dp, SpeedAlertRed),
                        modifier = Modifier.size(if (compact) 24.dp else 32.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "$speedLimitKmh",
                                color = Color.Black,
                                fontWeight = FontWeight.Black,
                                fontSize = if (compact) 10.sp else 12.sp
                            )
                        }
                    }

                    // RPM Pill
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Text(
                            text = "${engineRpm} RPM",
                            fontSize = if (compact) 9.sp else 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (engineRpm >= 5800) SpeedAlertRed else Color.LightGray,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }

        // Overspeed Banner if exceeding limit
        if (isOverSpeed) {
            Surface(
                color = SpeedAlertRed.copy(alpha = 0.2f),
                shape = RoundedCornerShape(6.dp),
                border = BorderStroke(1.dp, SpeedAlertRed),
                modifier = Modifier
                    .padding(horizontal = 6.dp, vertical = 1.dp)
                    .testTag("overspeed_alert_banner")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Warning",
                        tint = SpeedAlertRed,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "LIMIT EXCEEDED (+${currentSpeedKmh - speedLimitKmh} $speedUnitLabel)",
                        color = SpeedAlertRed,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }

        // Speed adjustment controls (simulates cruise control throttle)
        Row(
            modifier = Modifier.padding(top = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier
                    .size(if (compact) 26.dp else 36.dp)
                    .clickable { onAdjustSpeed((currentSpeedKmh - 5).coerceAtLeast(0)) }
                    .testTag("speed_decrease_button")
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Remove, contentDescription = "Decrease", tint = Color.White, modifier = Modifier.size(if (compact) 14.dp else 18.dp))
                }
            }

            Surface(
                shape = RoundedCornerShape(4.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)),
                modifier = Modifier.padding(horizontal = 2.dp)
            ) {
                Text(
                    text = "CRUISE",
                    fontSize = 8.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.5.sp,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }

            Surface(
                shape = RoundedCornerShape(6.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier
                    .size(if (compact) 26.dp else 36.dp)
                    .clickable { onAdjustSpeed((currentSpeedKmh + 5).coerceAtMost(maxGaugeSpeed.toInt())) }
                    .testTag("speed_increase_button")
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Add, contentDescription = "Increase", tint = Color.White, modifier = Modifier.size(if (compact) 14.dp else 18.dp))
                }
            }
        }
    }
}
