package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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

@Composable
fun SpeedometerGauge(
    currentSpeedKmh: Int,
    speedLimitKmh: Int,
    isOverSpeed: Boolean,
    onAdjustSpeed: (Int) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val animatedSpeed by animateFloatAsState(
        targetValue = currentSpeedKmh.toFloat(),
        animationSpec = tween(durationMillis = 400),
        label = "speed"
    )

    val gaugeColor by animateColorAsState(
        targetValue = if (isOverSpeed) SpeedAlertRed else MaterialTheme.colorScheme.primary,
        animationSpec = tween(300),
        label = "gaugeColor"
    )

    val secondaryColor = MaterialTheme.colorScheme.secondary

    Column(
        modifier = modifier.testTag("speedometer_container"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(240.dp)
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokeWidth = 16.dp.toPx()
                val radius = (size.minDimension - strokeWidth) / 2
                val center = Offset(size.width / 2, size.height / 2)
                val startAngle = 135f
                val sweepAngle = 270f

                // Background track arc
                drawArc(
                    color = Color.DarkGray.copy(alpha = 0.4f),
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    topLeft = Offset(center.x - radius, center.y - radius),
                    size = Size(radius * 2, radius * 2),
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )

                // Speed limit marker position
                val maxGaugeSpeed = 160f
                val speedFraction = (animatedSpeed / maxGaugeSpeed).coerceIn(0f, 1f)
                val currentSweep = sweepAngle * speedFraction

                // Active speed arc
                drawArc(
                    brush = Brush.sweepGradient(
                        0.0f to secondaryColor,
                        0.7f to gaugeColor,
                        1.0f to if (isOverSpeed) SpeedAlertRed else gaugeColor
                    ),
                    startAngle = startAngle,
                    sweepAngle = currentSweep,
                    useCenter = false,
                    topLeft = Offset(center.x - radius, center.y - radius),
                    size = Size(radius * 2, radius * 2),
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )

                // Needle indicator dot
                val needleAngleRad = Math.toRadians((startAngle + currentSweep).toDouble())
                val needleX = center.x + radius * cos(needleAngleRad).toFloat()
                val needleY = center.y + radius * sin(needleAngleRad).toFloat()
                drawCircle(
                    color = Color.White,
                    radius = 9.dp.toPx(),
                    center = Offset(needleX, needleY)
                )
                drawCircle(
                    color = gaugeColor,
                    radius = 5.dp.toPx(),
                    center = Offset(needleX, needleY)
                )
            }

            // Digital Speed Readout in Center
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "$currentSpeedKmh",
                    fontSize = 54.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = if (isOverSpeed) SpeedAlertRed else MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.testTag("digital_speed_text")
                )
                Text(
                    text = "KM/H",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Speed Limit Badge (Standard automotive European / Indian circular sign)
                Surface(
                    shape = CircleShape,
                    color = Color.White,
                    border = androidx.compose.foundation.BorderStroke(3.dp, SpeedAlertRed),
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "$speedLimitKmh",
                            color = Color.Black,
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }

        // Overspeed Warning Banner
        if (isOverSpeed) {
            Surface(
                color = SpeedAlertRed.copy(alpha = 0.2f),
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, SpeedAlertRed),
                modifier = Modifier
                    .padding(horizontal = 12.dp, vertical = 4.dp)
                    .testTag("overspeed_alert_banner")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Speed warning",
                        tint = SpeedAlertRed,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "SPEED LIMIT EXCEEDED (+${currentSpeedKmh - speedLimitKmh} KM/H)",
                        color = SpeedAlertRed,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }

        // Quick Speed adjustment buttons (Simulates vehicle throttle / speed input)
        Row(
            modifier = Modifier.padding(top = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier
                    .size(48.dp)
                    .clickable { onAdjustSpeed((currentSpeedKmh - 5).coerceAtLeast(0)) }
                    .testTag("speed_decrease_button")
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Remove,
                        contentDescription = "Decrease speed",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.padding(horizontal = 4.dp)
            ) {
                Text(
                    text = "GPS Cruise",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }

            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier
                    .size(48.dp)
                    .clickable { onAdjustSpeed((currentSpeedKmh + 5).coerceAtMost(160)) }
                    .testTag("speed_increase_button")
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Increase speed",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}
