package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Work
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.launcher.QuickAppsState
import com.example.ui.components.AddressLocationWidget
import com.example.ui.components.LargeSpeedometerCluster
import com.example.ui.components.QuickAppsBar
import com.example.ui.components.WeatherWidget
import com.example.ui.theme.SpeedAlertRed
import com.example.ui.viewmodel.CarHeadUnitViewModel
import com.example.ui.viewmodel.CarScreen

/**
 * Theme 1: Cockpit Modern Principal Screen:
 * - Left Pane: Large hero speedometer cluster (290dp) + RPM & Gear
 * - Right Pane: Dedicated Navigation & Route Guidance Card (Media line removed completely)
 * - Weather & Address Location Strip
 * - Customizable Quick Apps Bar with Folders & Edit Mode
 */
@Composable
fun DashboardModernTheme(
    viewModel: CarHeadUnitViewModel,
    quickAppsState: QuickAppsState,
    onExpandSpeedometer: () -> Unit,
    modifier: Modifier = Modifier
) {
    val navState by viewModel.navEngine.navState.collectAsState()
    val telemetry by viewModel.obdManager.telemetry.collectAsState()
    val installedApps by viewModel.appsManager.installedApps.collectAsState()

    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // DUAL-PANE COCKPIT DASHBOARD
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // LEFT PANE: COMPACT SPEEDOMETER CLUSTER
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier
                    .weight(1.05f)
                    .fillMaxHeight()
                    .testTag("home_speedometer_pane")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Header: Brand & Fullscreen Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color.White.copy(alpha = 0.12f),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.35f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
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

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                            modifier = Modifier
                                .clickable { onExpandSpeedometer() }
                                .testTag("toggle_full_speedometer_mode")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Fullscreen, contentDescription = "Full Gauge", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(3.dp))
                                Text("EXPAND", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }

                    // THE HERO COMPACT SPEEDOMETER CLUSTER
                    LargeSpeedometerCluster(
                        currentSpeedKmh = navState.currentSpeedKmh,
                        speedLimitKmh = navState.speedLimitKmh,
                        isOverSpeed = navState.isOverSpeed,
                        engineRpm = telemetry.engineRpm,
                        gear = "D",
                        isKmh = viewModel.uiState.collectAsState().value.speedUnitKmh,
                        compact = true,
                        onAdjustSpeed = { newSpeed ->
                            viewModel.navEngine.updateCurrentSpeed(newSpeed)
                        }
                    )

                    // BOTTOM ENGINE TELEMETRY BAR
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("COOLANT 88°C", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color.LightGray)
                            Text("BATT 14.2V", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color(0xFF00E676))
                            Text("ECO 22.8 KM/L", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = quickAppsState.textColorGamma.color)
                        }
                    }
                }
            }

            // RIGHT PANE: DEDICATED LIVE GPS LOCATION CARD (ROUTE GUIDANCE DISABLED)
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier
                    .weight(1.35f)
                    .fillMaxHeight()
                    .testTag("home_navigation_card")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(10.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Navigation, contentDescription = "GPS", tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("LIVE GPS LOCATION", fontSize = 11.sp, fontWeight = FontWeight.Normal, color = Color.White, letterSpacing = 0.5.sp)
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.4f)),
                                modifier = Modifier.clickable { viewModel.navigateTo(CarScreen.SPLIT_VIEW) }
                            ) {
                                Text("SPLIT (MAP+YT)", fontSize = 8.sp, fontWeight = FontWeight.Normal, color = Color.White, modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp))
                            }

                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.4f)),
                                modifier = Modifier.clickable { viewModel.appsManager.launchNavigationApp() }
                            ) {
                                Text("MAPS APP", fontSize = 8.sp, fontWeight = FontWeight.Normal, color = Color.White, modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp))
                            }
                        }
                    }

                    // Live GPS Position Banner
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.White.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Navigation, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = navState.currentRoad,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Normal,
                                    color = Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "28.4595° N, 77.0266° E • Altitude 216m",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Normal,
                                    color = Color.LightGray
                                )
                            }
                        }
                    }

                    // Live GPS Telemetry Grid
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("SATELLITES", fontSize = 8.sp, fontWeight = FontWeight.Normal, color = Color.Gray)
                                Text("11 SATS", fontSize = 10.sp, fontWeight = FontWeight.Normal, color = Color.White)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("ACCURACY", fontSize = 8.sp, fontWeight = FontWeight.Normal, color = Color.Gray)
                                Text("±1.8 METERS", fontSize = 10.sp, fontWeight = FontWeight.Normal, color = Color.White)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("HEADING", fontSize = 8.sp, fontWeight = FontWeight.Normal, color = Color.Gray)
                                Text("NW 315°", fontSize = 10.sp, fontWeight = FontWeight.Normal, color = Color.White)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("GPS STATUS", fontSize = 8.sp, fontWeight = FontWeight.Normal, color = Color.Gray)
                                Text("3D FIX LOCKED", fontSize = 10.sp, fontWeight = FontWeight.Normal, color = Color.White)
                            }
                        }
                    }

                    // Direct Action: Launch System Google Maps App
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.3f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.appsManager.launchNavigationApp() }
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Navigation, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("LAUNCH SYSTEM GOOGLE MAPS (SIGNED IN)", fontSize = 10.sp, fontWeight = FontWeight.Normal, color = Color.White)
                        }
                    }
                }
            }
        }

        // WEATHER & ADDRESS LOCATION STRIP (Icon row removed for maximum screen space)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            AddressLocationWidget(
                address = quickAppsState.address,
                tint = quickAppsState.textColorGamma.color,
                modifier = Modifier.weight(1f)
            )
            WeatherWidget(
                weather = quickAppsState.weather,
                tint = quickAppsState.textColorGamma.color,
                modifier = Modifier.weight(1f)
            )
        }
    }
}
