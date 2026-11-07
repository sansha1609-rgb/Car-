package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Flag
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
import com.example.ui.components.AnalogSpeedometerWidget
import com.example.ui.components.QuickAppsBar
import com.example.ui.components.WeatherWidget
import com.example.ui.theme.SpeedAlertRed
import com.example.ui.viewmodel.CarHeadUnitViewModel
import com.example.ui.viewmodel.CarScreen

/**
 * Theme 2: Classic Analog Gauges & Sport Dashboard:
 * - Dual analog circular dials (Speedometer on Left, Tachometer / Media on Right)
 * - Center Console: Analog Chronometer (tap for screensaver), Car brand logo, Trip movement/stop time
 * - Audio visualizer and quick apps dock
 */
@Composable
fun DashboardClassicAnalogTheme(
    viewModel: CarHeadUnitViewModel,
    quickAppsState: QuickAppsState,
    modifier: Modifier = Modifier
) {
    val navState by viewModel.navEngine.navState.collectAsState()
    val telemetry by viewModel.obdManager.telemetry.collectAsState()
    val computerMetrics by viewModel.onBoardComputer.metrics.collectAsState()
    val installedApps by viewModel.appsManager.installedApps.collectAsState()

    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // TOP 3-PANE ANALOG CLUSTER ROW
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // LEFT: ANALOG SPEEDOMETER DIAL
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("SPEEDOMETER", fontSize = 10.sp, fontWeight = FontWeight.Black, color = quickAppsState.textColorGamma.color)

                    Box(modifier = Modifier.size(125.dp), contentAlignment = Alignment.Center) {
                        AnalogSpeedometerWidget(
                            speed = navState.currentSpeedKmh.toFloat(),
                            maxSpeed = 180f,
                            tint = quickAppsState.textColorGamma.color,
                            modifier = Modifier.fillMaxSize()
                        )
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${navState.currentSpeedKmh}",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace,
                                color = Color.White
                            )
                            Text("KM/H", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = quickAppsState.textColorGamma.color)
                        }
                    }

                    // Accel 0-60 & Top Speed pill
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color.White.copy(alpha = 0.06f)
                        ) {
                            Row(modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Flag, contentDescription = null, tint = SpeedAlertRed, modifier = Modifier.size(10.dp))
                                Spacer(modifier = Modifier.width(2.dp))
                                Text("0-60: ${computerMetrics.accel0To60.timeSeconds}s", fontSize = 8.sp, color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color.White.copy(alpha = 0.06f)
                        ) {
                            Text("MAX: ${computerMetrics.maxSpeedKmh.toInt()}k", fontSize = 8.sp, color = Color.Gray, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                        }
                    }
                }
            }

            // CENTER: ANALOG CHRONO & CAR BRAND BADGE
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier
                    .weight(1.05f)
                    .fillMaxHeight()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Car Logo & Model
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

                    // Analog Chronometer (Click opens Screensaver)
                    AnalogClockWidget(
                        tint = quickAppsState.textColorGamma.color,
                        modifier = Modifier.size(110.dp),
                        onClick = { viewModel.setClockScreenSaverOpen(true) }
                    )

                    // Movement vs Stop time
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("RUN", fontSize = 7.sp, color = Color.Gray)
                            Text(viewModel.onBoardComputer.formatDuration(computerMetrics.operatingTimeSeconds), fontSize = 9.sp, fontWeight = FontWeight.Black, color = Color.White)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("MOVING", fontSize = 7.sp, color = Color.Gray)
                            Text(viewModel.onBoardComputer.formatDuration(computerMetrics.movingTimeSeconds), fontSize = 9.sp, fontWeight = FontWeight.Black, color = Color(0xFF00E676))
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("STOPS", fontSize = 7.sp, color = Color.Gray)
                            Text(viewModel.onBoardComputer.formatDuration(computerMetrics.stopTimeSeconds), fontSize = 9.sp, fontWeight = FontWeight.Black, color = Color(0xFFFF9100))
                        }
                    }
                }
            }

            // RIGHT: SPORT TACHOMETER & ENGINE TELEMETRY (MEDIA LINE REMOVED)
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clickable { viewModel.navigateTo(CarScreen.DIAGNOSTICS) }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.DirectionsCar, contentDescription = null, tint = quickAppsState.textColorGamma.color, modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("TACHOMETER", fontSize = 10.sp, fontWeight = FontWeight.Black, color = quickAppsState.textColorGamma.color)
                        }
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0xFF00E676).copy(alpha = 0.15f),
                            border = BorderStroke(1.dp, Color(0xFF00E676).copy(alpha = 0.4f))
                        ) {
                            Text("SPORT D", fontSize = 8.sp, fontWeight = FontWeight.Black, color = Color(0xFF00E676), modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                        }
                    }

                    // RPM & Gear Center
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Surface(
                            shape = CircleShape,
                            color = quickAppsState.textColorGamma.color.copy(alpha = 0.15f),
                            border = BorderStroke(1.5.dp, quickAppsState.textColorGamma.color),
                            modifier = Modifier.size(52.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("D", fontSize = 28.sp, fontWeight = FontWeight.Black, color = Color.White)
                            }
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text("${telemetry.engineRpm} RPM", fontSize = 13.sp, fontWeight = FontWeight.Black, color = Color.White)
                    }

                    // Engine Telemetry Metrics (Coolant, Battery, Throttle, Eco)
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 6.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("TEMP", fontSize = 7.sp, color = Color.Gray)
                                Text("${telemetry.coolantTempC}°C", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("BATT", fontSize = 7.sp, color = Color.Gray)
                                Text("${telemetry.batteryVoltage}V", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF00E676))
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("LOAD", fontSize = 7.sp, color = Color.Gray)
                                Text("${telemetry.throttlePercent}%", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = quickAppsState.textColorGamma.color)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("ECO", fontSize = 7.sp, color = Color.Gray)
                                Text("${telemetry.instantEfficiencyKmpl}", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                }
            }
        }

        // WEATHER & ADDRESS LOCATION STRIP
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
