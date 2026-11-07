package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AvTimer
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.launcher.LauncherScreenTheme
import com.example.ui.components.CarTopBar
import com.example.ui.components.LargeSpeedometerCluster
import com.example.ui.viewmodel.CarHeadUnitViewModel

/**
 * Principal Screen for Car Launcher:
 * - 3 Subjects / Themes of the Principal Screen on a choice by default:
 *   1. Cockpit Modern (Large central speedometer cluster, dual cards)
 *   2. Classic Analog Gauges (Dual analog dials, tachometer, chronometer)
 *   3. Agama Tech Grid (Signature circular automotive hub, weather, trip)
 * - Clock Screen Saver triggered by tapping on time
 * - On-Board Computer Slide menu triggered by rounded button or right edge pull
 * - Customizable quick apps bar with folders, infinite scroll, and edit mode
 */
@Composable
fun DashboardScreen(
    viewModel: CarHeadUnitViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val quickAppsState by viewModel.quickAppsManager.state.collectAsState()
    val navState by viewModel.navEngine.navState.collectAsState()
    val sysStatus by viewModel.systemStatusManager.status.collectAsState()
    val installedApps by viewModel.appsManager.installedApps.collectAsState()

    var isFullSpeedometerMode by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 6.dp, vertical = 2.dp)
            .testTag("dashboard_screen")
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // MAIN STAGE (Top status bar hidden for immersive display)
            if (isFullSpeedometerMode) {
                FullSpeedometerCockpitView(
                    viewModel = viewModel,
                    onExitFull = { isFullSpeedometerMode = false },
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    when (quickAppsState.launcherTheme) {
                        LauncherScreenTheme.COCKPIT_MODERN -> {
                            DashboardModernTheme(
                                viewModel = viewModel,
                                quickAppsState = quickAppsState,
                                onExpandSpeedometer = { isFullSpeedometerMode = true }
                            )
                        }
                        LauncherScreenTheme.CLASSIC_ANALOG -> {
                            DashboardClassicAnalogTheme(
                                viewModel = viewModel,
                                quickAppsState = quickAppsState
                            )
                        }
                        LauncherScreenTheme.AGAMA_TECH -> {
                            DashboardAgamaTechTheme(
                                viewModel = viewModel,
                                quickAppsState = quickAppsState
                            )
                        }
                    }
                }
            }
        }

        // FLOATING RIGHT-EDGE PULL TAB FOR ON-BOARD COMPUTER
        Surface(
            shape = RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp),
            color = quickAppsState.textColorGamma.color,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .clickable { viewModel.setOnBoardComputerOpen(true) }
                .padding(vertical = 4.dp)
                .testTag("floating_slide_computer_tab")
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 5.dp, vertical = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(Icons.Default.AvTimer, contentDescription = "Trip Computer", tint = Color.Black, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.height(2.dp))
                Text("T\nR\nI\nP", fontSize = 9.sp, fontWeight = FontWeight.Black, color = Color.Black, lineHeight = 10.sp)
            }
        }

        // ON-BOARD COMPUTER SLIDE DRAWER
        OnBoardComputerSlideDrawer(
            isOpen = uiState.isOnBoardComputerOpen,
            onClose = { viewModel.setOnBoardComputerOpen(false) },
            computerManager = viewModel.onBoardComputer,
            speedUnitKmh = uiState.speedUnitKmh,
            onToggleSpeedUnit = { viewModel.toggleSpeedUnit() },
            modifier = Modifier.align(Alignment.CenterEnd)
        )

        // CLOCK SCREEN SAVER DIALOG (Tapping on hours)
        ClockScreenSaverDialog(
            isOpen = uiState.isClockScreenSaverOpen,
            onDismiss = { viewModel.setClockScreenSaverOpen(false) },
            status = sysStatus,
            speedKmh = navState.currentSpeedKmh
        )

        // APP FOLDER DIALOG
        AppFolderDialog(
            folder = uiState.activeFolder,
            allInstalledApps = installedApps,
            onDismiss = { viewModel.setActiveFolder(null) },
            onLaunchApp = { app -> viewModel.appsManager.launchApp(app) },
            onAddAppToFolder = { folderId, pkg ->
                viewModel.quickAppsManager.addAppToFolder(folderId, pkg)
                viewModel.showNotification("Added app to folder")
            }
        )

        // APP ACTION SHEET DIALOG (Long press on apps)
        AppActionDialog(
            app = uiState.selectedActionApp,
            folders = quickAppsState.folders,
            onDismiss = { viewModel.setSelectedActionApp(null) },
            onLaunch = { app -> viewModel.appsManager.launchApp(app) },
            onAddToHome = { app ->
                viewModel.quickAppsManager.addAppToHome(app)
                viewModel.showNotification("Added ${app.label} to Principal Screen")
            },
            onAddToFolder = { folderId, pkg ->
                viewModel.quickAppsManager.addAppToFolder(folderId, pkg)
                viewModel.showNotification("Added to folder")
            },
            onUninstall = { pkg -> viewModel.quickAppsManager.requestUninstallApp(pkg) },
            onOpenInfo = { pkg -> viewModel.quickAppsManager.openAppDetails(pkg) }
        )
    }
}

/**
 * Fullscreen Dedicated Instrument Cluster View with Massive Speedometer
 */
@Composable
private fun FullSpeedometerCockpitView(
    viewModel: CarHeadUnitViewModel,
    onExitFull: () -> Unit,
    modifier: Modifier = Modifier
) {
    val navState by viewModel.navEngine.navState.collectAsState()
    val telemetry by viewModel.obdManager.telemetry.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val quickAppsState by viewModel.quickAppsManager.state.collectAsState()

    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = modifier.testTag("full_speedometer_cockpit")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "DIGITAL INSTRUMENT COCKPIT • ${quickAppsState.carLogo.displayName.uppercase()}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    color = quickAppsState.textColorGamma.color,
                    letterSpacing = 1.sp
                )

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    modifier = Modifier.clickable(onClick = onExitFull)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.FullscreenExit, contentDescription = "Exit Full", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("DASHBOARD VIEW", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }

            LargeSpeedometerCluster(
                currentSpeedKmh = navState.currentSpeedKmh,
                speedLimitKmh = navState.speedLimitKmh,
                isOverSpeed = navState.isOverSpeed,
                engineRpm = telemetry.engineRpm,
                gear = "D",
                isKmh = uiState.speedUnitKmh,
                compact = false,
                onAdjustSpeed = { newSpeed ->
                    viewModel.navEngine.updateCurrentSpeed(newSpeed)
                },
                modifier = Modifier.padding(vertical = 4.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                listOf(
                    "RPM" to "${telemetry.engineRpm}",
                    "COOLANT" to "${telemetry.coolantTempC}°C",
                    "BATTERY" to "${telemetry.batteryVoltage}V",
                    "EFFICIENCY" to "${telemetry.instantEfficiencyKmpl} km/L",
                    "THROTTLE" to "${telemetry.throttlePercent}%",
                    "ODOMETER" to "${telemetry.totalOdometerKm} KM"
                ).forEach { (label, value) ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(label, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                        Text(value, fontSize = 13.sp, fontWeight = FontWeight.Black, color = Color.White)
                    }
                }
            }
        }
    }
}
