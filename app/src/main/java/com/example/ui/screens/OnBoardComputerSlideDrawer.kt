package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AvTimer
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hardware.OnBoardComputerManager
import com.example.hardware.TripTimeFilter
import com.example.ui.theme.SpeedAlertRed

/**
 * On-Board Computer Slide Menu Drawer:
 * Opens via right-edge rounded button or edge swipe.
 * Displays live automotive metrics:
 * - Current speed, passable distance, average speed, max speed
 * - Operating time, moving time, stop time
 * - Acceleration 0-60 km/h, 0-100 km/h, 1/4 mile drag time & trap speed
 * - Filter period: Trip, Today, Week, Month, All Time
 * - Reset / drop data for a trip
 * - KM/H vs MPH toggle
 */
@Composable
fun OnBoardComputerSlideDrawer(
    isOpen: Boolean,
    onClose: () -> Unit,
    computerManager: OnBoardComputerManager,
    speedUnitKmh: Boolean,
    onToggleSpeedUnit: () -> Unit,
    modifier: Modifier = Modifier
) {
    val metrics by computerManager.metrics.collectAsState()

    AnimatedVisibility(
        visible = isOpen,
        enter = slideInHorizontally(initialOffsetX = { it }),
        exit = slideOutHorizontally(targetOffsetX = { it }),
        modifier = modifier
    ) {
        Surface(
            color = Color(0xFF0C1322).copy(alpha = 0.98f),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)),
            shape = RoundedCornerShape(topStart = 14.dp, bottomStart = 14.dp),
            modifier = Modifier
                .fillMaxHeight()
                .width(360.dp)
                .testTag("onboard_computer_slide_drawer")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(10.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.18f),
                            modifier = Modifier.size(34.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.AvTimer, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("ON-BOARD COMPUTER", fontSize = 13.sp, fontWeight = FontWeight.Black, color = Color.White, letterSpacing = 1.sp)
                            Text("Real-Time Telemetry & Drag Times", fontSize = 10.sp, color = Color.Gray)
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        // KM/H vs MPH unit toggle
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.18f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)),
                            modifier = Modifier
                                .clickable { onToggleSpeedUnit() }
                                .testTag("toggle_speed_unit_btn")
                        ) {
                            Text(
                                text = if (speedUnitKmh) "KM/H" else "MPH",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

                        IconButton(onClick = onClose, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Time Filter Tabs
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    TripTimeFilter.values().forEach { filter ->
                        val isSelected = metrics.currentFilter == filter
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.06f),
                            border = BorderStroke(1.dp, if (isSelected) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.12f)),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { computerManager.selectTimeFilter(filter) }
                        ) {
                            Text(
                                text = when (filter) {
                                    TripTimeFilter.TRIP -> "Trip"
                                    TripTimeFilter.TODAY -> "Today"
                                    TripTimeFilter.WEEK -> "Week"
                                    TripTimeFilter.MONTH -> "Month"
                                    TripTimeFilter.ALL_TIME -> "All"
                                },
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                                color = if (isSelected) Color.Black else Color.White,
                                modifier = Modifier.padding(vertical = 5.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Scrollable metrics list
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Current Speed & Passable Distance
                    val unitSpeed = if (speedUnitKmh) "km/h" else "mph"
                    val unitDist = if (speedUnitKmh) "km" else "mi"
                    val speedVal = if (speedUnitKmh) metrics.currentSpeedKmh else metrics.currentSpeedKmh * 0.621371f
                    val distVal = if (speedUnitKmh) metrics.passableDistanceKm else metrics.passableDistanceKm * 0.621371f
                    val avgSpeedVal = if (speedUnitKmh) metrics.averageSpeedKmh else metrics.averageSpeedKmh * 0.621371f
                    val maxSpeedVal = if (speedUnitKmh) metrics.maxSpeedKmh else metrics.maxSpeedKmh * 0.621371f

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        MetricCard(
                            label = "CURRENT SPEED",
                            value = "%.0f".format(speedVal),
                            unit = unitSpeed,
                            icon = Icons.Default.Speed,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.weight(1f)
                        )
                        MetricCard(
                            label = "PASSABLE DISTANCE",
                            value = "%.1f".format(distVal),
                            unit = unitDist,
                            icon = Icons.Default.Route,
                            tint = Color(0xFF00E676),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Average Rate & Maximum Speed
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        MetricCard(
                            label = "AVERAGE SPEED",
                            value = "%.1f".format(avgSpeedVal),
                            unit = unitSpeed,
                            icon = Icons.Default.AvTimer,
                            tint = Color(0xFFFF9100),
                            modifier = Modifier.weight(1f)
                        )
                        MetricCard(
                            label = "MAXIMUM SPEED",
                            value = "%.0f".format(maxSpeedVal),
                            unit = unitSpeed,
                            icon = Icons.Default.Speed,
                            tint = SpeedAlertRed,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Operating Times
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White.copy(alpha = 0.05f),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Timer, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("OPERATING TIME PROFILE", fontSize = 10.sp, fontWeight = FontWeight.Black, color = Color.White)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                TimeColumn("General Run", computerManager.formatDuration(metrics.operatingTimeSeconds), MaterialTheme.colorScheme.primary)
                                TimeColumn("Moving", computerManager.formatDuration(metrics.movingTimeSeconds), Color(0xFF00E676))
                                TimeColumn("Stops / Idle", computerManager.formatDuration(metrics.stopTimeSeconds), Color(0xFFFF9100))
                            }
                        }
                    }

                    // Acceleration Drag Tests (0-60, 0-100, 1/4 mile)
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White.copy(alpha = 0.05f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Flag, contentDescription = null, tint = SpeedAlertRed, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("PERFORMANCE DRAG ACCELERATION", fontSize = 10.sp, fontWeight = FontWeight.Black, color = Color.White)
                            }
                            Spacer(modifier = Modifier.height(8.dp))

                            // 0-60 km/h
                            AccelRow(
                                title = "0 - 60 km/h",
                                result = if (metrics.accel0To60.isCompleted) "%.2f s".format(metrics.accel0To60.timeSeconds) else "Ready",
                                statusColor = if (metrics.accel0To60.isCompleted) Color(0xFF00E676) else Color.Gray
                            )

                            // 0-100 km/h
                            AccelRow(
                                title = "0 - 100 km/h",
                                result = if (metrics.accel0To100.isCompleted) "%.2f s".format(metrics.accel0To100.timeSeconds) else "Ready",
                                statusColor = if (metrics.accel0To100.isCompleted) Color(0xFF00E676) else Color.Gray
                            )

                            // 1/4 Mile (402 m)
                            AccelRow(
                                title = "1/4 Mile (402m)",
                                result = if (metrics.quarterMile.isCompleted) "%.2f s @ %.0f km/h".format(metrics.quarterMile.timeSeconds, metrics.quarterMile.trapSpeedKmh) else "Ready",
                                statusColor = if (metrics.quarterMile.isCompleted) MaterialTheme.colorScheme.primary else Color.Gray
                            )
                        }
                    }

                    // Fuel & Efficiency
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White.copy(alpha = 0.05f),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.LocalGasStation, contentDescription = null, tint = Color(0xFF00E676), modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("EFFICIENCY", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                            Text(
                                text = "%.1f L • %.1f km/L".format(metrics.fuelConsumedLiters, metrics.fuelEfficiencyKmpl),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF00E676)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Bottom Action: Drop / Reset Trip Data
                Button(
                    onClick = { computerManager.resetTripData() },
                    colors = ButtonDefaults.buttonColors(containerColor = SpeedAlertRed.copy(alpha = 0.18f)),
                    border = BorderStroke(1.dp, SpeedAlertRed.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(42.dp)
                        .testTag("reset_trip_data_btn")
                ) {
                    Icon(Icons.Default.DeleteSweep, contentDescription = null, tint = SpeedAlertRed, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("DROP / RESET DATA FOR TRIP", color = SpeedAlertRed, fontSize = 11.sp, fontWeight = FontWeight.Black)
                }
            }
        }
    }
}

@Composable
private fun MetricCard(
    label: String,
    value: String,
    unit: String,
    icon: ImageVector,
    tint: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color.White.copy(alpha = 0.05f),
        border = BorderStroke(1.dp, tint.copy(alpha = 0.25f)),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(label, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(value, fontSize = 20.sp, fontWeight = FontWeight.Black, color = Color.White)
                Spacer(modifier = Modifier.width(4.dp))
                Text(unit, fontSize = 10.sp, color = tint, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun TimeColumn(label: String, time: String, tint: Color) {
    Column {
        Text(label, fontSize = 9.sp, color = Color.Gray)
        Text(time, fontSize = 12.sp, fontWeight = FontWeight.Black, color = tint)
    }
}

@Composable
private fun AccelRow(title: String, result: String, statusColor: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, fontSize = 11.sp, color = Color.White)
        Text(result, fontSize = 11.sp, fontWeight = FontWeight.Black, color = statusColor)
    }
}
