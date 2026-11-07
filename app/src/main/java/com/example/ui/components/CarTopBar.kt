package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AvTimer
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BluetoothConnected
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.Brightness7
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Wifi
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.launcher.LauncherScreenTheme
import com.example.launcher.SystemStatusManager
import com.example.ui.theme.SpeedAlertRed
import com.example.ui.viewmodel.CarHeadUnitViewModel

/**
 * Ultra-Compact & Short Status Bar for Landscape Car Head Unit (~36dp height):
 * - Left: Digital Clock & Date (Tap opens interactive Screen Saver) + Brand emblem
 * - Center: Seamless Inline 3-Theme Switcher (Cockpit, Analog, Agama)
 * - Right: Compact Telemetry & Status Badges (Batt, Fuel, BT, Wi-Fi, GPS) + Night/Voice/Computer buttons
 */
@Composable
fun CarTopBar(
    viewModel: CarHeadUnitViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val sysStatus by viewModel.systemStatusManager.status.collectAsState()
    val telemetry by viewModel.obdManager.telemetry.collectAsState()
    val quickAppsState by viewModel.quickAppsManager.state.collectAsState()

    Surface(
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        shape = RoundedCornerShape(10.dp),
        modifier = modifier
            .fillMaxWidth()
            .height(36.dp)
            .testTag("car_top_bar")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // LEFT: Compact Clock, Date, and Car Brand Badge (Tap for Screen Saver)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .clickable { viewModel.setClockScreenSaverOpen(true) }
                    .padding(horizontal = 4.dp, vertical = 2.dp)
                    .testTag("top_bar_clock_section")
            ) {
                Text(
                    text = sysStatus.timeFormatted,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    color = quickAppsState.textColorGamma.color,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = ":${sysStatus.timeSecondsFormatted}",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = quickAppsState.textColorGamma.color.copy(alpha = 0.8f)
                )

                Spacer(modifier = Modifier.width(6.dp))

                Text(
                    text = sysStatus.dateFormatted,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Brand Pill Badge
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = quickAppsState.textColorGamma.color.copy(alpha = 0.16f),
                    border = BorderStroke(1.dp, quickAppsState.textColorGamma.color.copy(alpha = 0.35f))
                ) {
                    Text(
                        text = quickAppsState.carLogo.displayName.uppercase(),
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Black,
                        color = quickAppsState.textColorGamma.color,
                        letterSpacing = 0.5.sp,
                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                    )
                }
            }

            // CENTER: Compact Inline Theme Switcher Tabs
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.testTag("top_bar_theme_pills")
            ) {
                LauncherScreenTheme.values().forEach { theme ->
                    val isSelected = quickAppsState.launcherTheme == theme
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (isSelected) quickAppsState.textColorGamma.color else Color.Transparent,
                        border = BorderStroke(
                            1.dp,
                            if (isSelected) quickAppsState.textColorGamma.color else MaterialTheme.colorScheme.outline
                        ),
                        modifier = Modifier
                            .clickable { viewModel.quickAppsManager.setLauncherTheme(theme) }
                            .testTag("theme_tab_${theme.name}")
                    ) {
                        Text(
                            text = when (theme) {
                                LauncherScreenTheme.COCKPIT_MODERN -> "COCKPIT"
                                LauncherScreenTheme.CLASSIC_ANALOG -> "ANALOG"
                                LauncherScreenTheme.AGAMA_TECH -> "AGAMA"
                            },
                            fontSize = 9.sp,
                            fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                            color = if (isSelected) Color.Black else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            // RIGHT: Compact Status Badges & Quick Action Controls
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.testTag("top_bar_status_icons")
            ) {
                // Alternator Voltage
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.DirectionsCar, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(2.dp))
                    Text("${telemetry.batteryVoltage}V", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                }

                // Fuel Efficiency
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Speed, contentDescription = null, tint = Color(0xFF00E676), modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(2.dp))
                    Text("${telemetry.instantEfficiencyKmpl}", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                }

                // Phone / BT
                Icon(
                    imageVector = if (sysStatus.isBluetoothConnected) Icons.Default.BluetoothConnected else Icons.Default.Bluetooth,
                    contentDescription = "Bluetooth",
                    tint = if (sysStatus.isBluetoothConnected) MaterialTheme.colorScheme.primary else Color.Gray,
                    modifier = Modifier.size(13.dp)
                )

                // Wi-Fi
                Icon(
                    imageVector = Icons.Default.Wifi,
                    contentDescription = "Wi-Fi",
                    tint = if (sysStatus.isWifiConnected) MaterialTheme.colorScheme.primary else Color.Gray,
                    modifier = Modifier.size(13.dp)
                )

                // GPS 3D
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "GPS",
                        tint = if (sysStatus.isGpsActive) Color(0xFF00E676) else SpeedAlertRed,
                        modifier = Modifier.size(13.dp)
                    )
                    Text("3D", fontSize = 8.sp, fontWeight = FontWeight.Black, color = if (sysStatus.isGpsActive) Color(0xFF00E676) else SpeedAlertRed)
                }

                // Battery / Power
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (sysStatus.isCharging) Icons.Default.BatteryChargingFull else Icons.Default.BatteryFull,
                        contentDescription = "Battery",
                        tint = if (sysStatus.isCharging) Color(0xFF00E676) else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(14.dp)
                    )
                    Text("${sysStatus.batteryPercent}%", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                }

                // Night Mode Toggle (compact circle)
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    modifier = Modifier
                        .size(24.dp)
                        .clickable { viewModel.toggleNightMode() }
                        .testTag("top_bar_night_mode_toggle")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (uiState.isNightMode) Icons.Default.Brightness4 else Icons.Default.Brightness7,
                            contentDescription = "Toggle Night Mode",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }

                // Voice Assistant (compact circle)
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .size(24.dp)
                        .clickable { viewModel.triggerVoiceCommand() }
                        .testTag("top_bar_voice_mic_button")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = "Voice Assistant",
                            tint = Color.Black,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }

                // Compact COMPUTER button
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = quickAppsState.textColorGamma.color.copy(alpha = 0.22f),
                    border = BorderStroke(1.dp, quickAppsState.textColorGamma.color),
                    modifier = Modifier
                        .height(24.dp)
                        .clickable { viewModel.setOnBoardComputerOpen(true) }
                        .testTag("top_bar_onboard_computer_button")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.AvTimer,
                            contentDescription = "On-Board Computer",
                            tint = quickAppsState.textColorGamma.color,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "COMP ▶",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

