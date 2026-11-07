package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import com.example.navigation.DestinationPreset
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AvTimer
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.launcher.QuickAppsState
import com.example.ui.components.AddressLocationWidget
import com.example.ui.components.AnalogClockWidget
import com.example.ui.components.QuickAppsBar
import com.example.ui.components.WeatherWidget
import com.example.ui.theme.SpeedAlertRed
import com.example.ui.viewmodel.CarHeadUnitViewModel
import com.example.ui.viewmodel.CarScreen

/**
 * Theme 3: Agama Tech Grid Automotive Launcher Layout:
 * - Iconic Central Circular Speed & Car Brand Hub
 * - Left Pane: Weather & Turn-by-Turn Navigation Guidance
 * - Right Pane: Dedicated On-Board Computer & Trip Telemetry (Media line removed completely)
 * - Bottom: Customizable Quick Apps Bar with Folders
 */
@Composable
fun DashboardAgamaTechTheme(
    viewModel: CarHeadUnitViewModel,
    quickAppsState: QuickAppsState,
    modifier: Modifier = Modifier
) {
    val navState by viewModel.navEngine.navState.collectAsState()
    val computerMetrics by viewModel.onBoardComputer.metrics.collectAsState()
    val installedApps by viewModel.appsManager.installedApps.collectAsState()

    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // MAIN STAGE: 3-COLUMN AGAMA GRID
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // LEFT COLUMN: WEATHER & NAVIGATION CARD
            Column(
                modifier = Modifier
                    .weight(1.05f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Weather Widget
                WeatherWidget(
                    weather = quickAppsState.weather,
                    tint = quickAppsState.textColorGamma.color,
                    modifier = Modifier.fillMaxWidth()
                )

                // Navigation Maneuver Card
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clickable { viewModel.navigateTo(CarScreen.NAVIGATION) }
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(6.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Navigation, contentDescription = null, tint = Color(0xFF00E676), modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("NAV", fontSize = 10.sp, fontWeight = FontWeight.Black, color = Color.White)
                            }
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = Color(0xFF00E676).copy(alpha = 0.15f),
                                border = BorderStroke(1.dp, Color(0xFF00E676).copy(alpha = 0.4f))
                            ) {
                                Text("MAPS", fontSize = 8.sp, fontWeight = FontWeight.Black, color = Color(0xFF00E676), modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp))
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Color(0xFF00E676), modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Text(
                                        text = navState.currentManeuver?.instruction ?: "Stay on course along Highway",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "${navState.distanceRemainingKm} km • ${navState.etaMinutes}m",
                                        fontSize = 9.sp,
                                        color = Color.Gray
                                    )
                                }
                            }
                        }

                        // Presets
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            viewModel.navEngine.presetDestinations.take(3).forEach { p ->
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable {
                                            viewModel.navEngine.startNavigation(p)
                                            viewModel.navigateTo(CarScreen.NAVIGATION)
                                        }
                                ) {
                                    Text(
                                        text = p.name.take(7),
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        modifier = Modifier.padding(vertical = 3.dp),
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // CENTER: AGAMA SIGNATURE SPEED & BRAND HUB
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, quickAppsState.textColorGamma.color.copy(alpha = 0.5f)),
                modifier = Modifier
                    .weight(1.15f)
                    .fillMaxHeight()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Car Brand Emblem
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color.White.copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.35f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_suzuki_logo),
                                contentDescription = "Suzuki Logo",
                                tint = Color.White,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "SUZUKI",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White,
                                letterSpacing = 1.sp
                            )
                        }
                    }

                    // Cyber Speed Ring (120dp compact)
                    Box(
                        modifier = Modifier
                            .size(118.dp)
                            .clip(CircleShape)
                            .clickable { viewModel.toggleSpeedUnit() },
                        contentAlignment = Alignment.Center
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFF070C16),
                            border = BorderStroke(3.dp, quickAppsState.textColorGamma.color),
                            modifier = Modifier.fillMaxSize()
                        ) {}

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${navState.currentSpeedKmh}",
                                fontSize = 36.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace,
                                color = Color.White
                            )
                            Text(
                                text = "KM/H",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = quickAppsState.textColorGamma.color
                            )
                        }
                    }

                    // Gear & Trip Distance
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("GEAR", fontSize = 7.sp, color = Color.Gray)
                            Text("D", fontSize = 13.sp, fontWeight = FontWeight.Black, color = quickAppsState.textColorGamma.color)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("TRIP", fontSize = 7.sp, color = Color.Gray)
                            Text("%.1f km".format(computerMetrics.passableDistanceKm), fontSize = 10.sp, fontWeight = FontWeight.Black, color = Color.White)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("AVG", fontSize = 7.sp, color = Color.Gray)
                            Text("%.0f".format(computerMetrics.averageSpeedKmh), fontSize = 10.sp, fontWeight = FontWeight.Black, color = Color(0xFF00E676))
                        }
                    }
                }
            }

            // RIGHT COLUMN: ON-BOARD COMPUTER & TRIP METRICS (MEDIA LINE REMOVED)
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier
                    .weight(1.05f)
                    .fillMaxHeight()
                    .clickable { viewModel.setOnBoardComputerOpen(true) }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Header: On-Board Computer title + Open Drawer indicator
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AvTimer, contentDescription = "Trip Computer", tint = quickAppsState.textColorGamma.color, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(5.dp))
                            Text("TRIP COMPUTER", fontSize = 10.sp, fontWeight = FontWeight.Black, color = quickAppsState.textColorGamma.color)
                        }

                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = quickAppsState.textColorGamma.color.copy(alpha = 0.15f),
                            border = BorderStroke(1.dp, quickAppsState.textColorGamma.color.copy(alpha = 0.3f))
                        ) {
                            Text("DETAILS", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = quickAppsState.textColorGamma.color, modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp))
                        }
                    }

                    // Acceleration Sprint Timers (0-60, 0-100, 1/4 Mile)
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(6.dp)) {
                            Text("ACCELERATION TIMERS", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("0-60 KM/H", fontSize = 7.sp, color = Color.LightGray)
                                    Text(
                                        text = if (computerMetrics.accel0To60.isCompleted) "${computerMetrics.accel0To60.timeSeconds}s" else "--.-s",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color(0xFF00E676)
                                    )
                                }
                                Column {
                                    Text("0-100 KM/H", fontSize = 7.sp, color = Color.LightGray)
                                    Text(
                                        text = if (computerMetrics.accel0To100.isCompleted) "${computerMetrics.accel0To100.timeSeconds}s" else "--.-s",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Black,
                                        color = quickAppsState.textColorGamma.color
                                    )
                                }
                                Column {
                                    Text("1/4 MILE", fontSize = 7.sp, color = Color.LightGray)
                                    Text(
                                        text = if (computerMetrics.quarterMile.isCompleted) "${computerMetrics.quarterMile.timeSeconds}s" else "--.-s",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }

                    // Trip telemetry statistics (Distance, Moving, Stops, Avg)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("DISTANCE", fontSize = 7.sp, color = Color.Gray)
                            Text("%.1f km".format(computerMetrics.passableDistanceKm), fontSize = 10.sp, fontWeight = FontWeight.Black, color = Color.White)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("MOVING", fontSize = 7.sp, color = Color.Gray)
                            Text(viewModel.onBoardComputer.formatDuration(computerMetrics.movingTimeSeconds), fontSize = 10.sp, fontWeight = FontWeight.Black, color = Color(0xFF00E676))
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("MAX SPD", fontSize = 7.sp, color = Color.Gray)
                            Text("${computerMetrics.maxSpeedKmh.toInt()} km/h", fontSize = 10.sp, fontWeight = FontWeight.Black, color = Color.White)
                        }
                    }

                    // Reset Trip Data Button
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.onBoardComputer.resetTripData() }
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Flag, contentDescription = null, tint = quickAppsState.textColorGamma.color, modifier = Modifier.size(11.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("RESET TRIP DATA", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
        }

        // ADDRESS LOCATION STRIP
        AddressLocationWidget(
            address = quickAppsState.address,
            tint = quickAppsState.textColorGamma.color,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
