package com.example.ui.screens

import android.provider.Settings
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.AvTimer
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Brightness6
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.material.icons.filled.Wifi
import com.example.launcher.CarBrandLogo
import com.example.launcher.LauncherScreenTheme
import com.example.launcher.QuickAppsState
import com.example.launcher.TextColorGamma
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.CarTopBar
import com.example.ui.theme.CarWallpaper
import com.example.ui.theme.CockpitTheme
import com.example.ui.viewmodel.CarHeadUnitViewModel
import com.example.ui.viewmodel.CarScreen

enum class SettingsTab(val title: String, val icon: ImageVector) {
    DISPLAY("Display & Brightness", Icons.Default.Brightness6),
    DAY_NIGHT("Dark / Light Mode", Icons.Default.DarkMode),
    THEME_COLORS("Themes & Gamma", Icons.Default.Palette),
    CAR_LOGO("Car Brand Logo", Icons.Default.DirectionsCar),
    QUICK_APPS_CONFIG("Quick Apps & Folders", Icons.Default.Apps),
    SCREEN_SAVER("Clock Screen Saver", Icons.Default.AvTimer),
    WALLPAPER("Wallpaper", Icons.Default.Wallpaper),
    DATE_TIME("Date & Time", Icons.Default.AvTimer),
    BLUETOOTH("Bluetooth", Icons.Default.Bluetooth),
    WIFI("Wi-Fi", Icons.Default.Wifi),
    SOUND("Sound & Mixing", Icons.AutoMirrored.Filled.VolumeUp),
    LAUNCHER("Home Launcher & Boot", Icons.Default.Home),
    ABOUT("About & Car Info", Icons.Default.Info)
}

/**
 * Settings Screen for 9-inch 1280x720 Car Head Unit:
 * - Display brightness
 * - Day/Night mode
 * - Theme
 * - Wallpaper
 * - Date & time
 * - Bluetooth
 * - Wi-Fi
 * - Sound
 * - Launcher settings
 * - About
 * Directly connects to actual system settings via Android Intents where permitted.
 */
@Composable
fun SettingsScreen(
    viewModel: CarHeadUnitViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val quickAppsState by viewModel.quickAppsManager.state.collectAsState()
    val sysStatus by viewModel.systemStatusManager.status.collectAsState()

    var selectedTab by remember { mutableStateOf(SettingsTab.DISPLAY) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 14.dp, vertical = 6.dp)
            .testTag("settings_screen")
    ) {
        // MAIN 2-PANE SETTINGS LAYOUT (Ideal for 1280x720 widescreen head units)
        Row(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // LEFT COLUMN: Category Selector
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier
                    .width(280.dp)
                    .fillMaxHeight()
            ) {
                val scrollState = rememberScrollState()
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp)
                        .verticalScroll(scrollState),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    SettingsTab.values().forEach { tab ->
                        val isSelected = tab == selectedTab
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else Color.Transparent,
                            border = BorderStroke(
                                1.dp,
                                if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .clickable { selectedTab = tab }
                                .testTag("settings_tab_${tab.name}")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = tab.icon,
                                    contentDescription = tab.title,
                                    tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = tab.title,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Black else FontWeight.SemiBold,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }

            // RIGHT COLUMN: Setting Controls Content Area
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                val contentScroll = rememberScrollState()
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(18.dp)
                        .verticalScroll(contentScroll),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    when (selectedTab) {
                        SettingsTab.DISPLAY -> DisplayBrightnessSection(viewModel, sysStatus.brightnessPercent)
                        SettingsTab.DAY_NIGHT -> DayNightSection(viewModel, uiState.isNightMode, uiState.isAutoNightMode)
                        SettingsTab.THEME_COLORS -> ThemesAndGammaSection(viewModel, quickAppsState)
                        SettingsTab.CAR_LOGO -> CarLogoSection(viewModel, quickAppsState.carLogo)
                        SettingsTab.QUICK_APPS_CONFIG -> QuickAppsConfigSection(viewModel, quickAppsState)
                        SettingsTab.SCREEN_SAVER -> ScreenSaverSection(viewModel)
                        SettingsTab.WALLPAPER -> WallpaperSection(viewModel, uiState.wallpaper)
                        SettingsTab.DATE_TIME -> DateTimeSection(viewModel, sysStatus.timeFormatted, sysStatus.dateFormatted)
                        SettingsTab.BLUETOOTH -> BluetoothSection(viewModel, sysStatus.isBluetoothOn, sysStatus.connectedDeviceName)
                        SettingsTab.WIFI -> WifiSection(viewModel, sysStatus.isWifiConnected, sysStatus.wifiSsid)
                        SettingsTab.SOUND -> SoundSection(viewModel, sysStatus.systemVolumePercent)
                        SettingsTab.LAUNCHER -> LauncherSection(viewModel, uiState.speedUnitKmh)
                        SettingsTab.ABOUT -> AboutSection(viewModel)
                    }
                }
            }
        }
    }
}

@Composable
private fun DisplayBrightnessSection(viewModel: CarHeadUnitViewModel, currentBrightness: Int) {
    var sliderValue by remember(currentBrightness) { mutableStateOf(currentBrightness.toFloat()) }

    Text("Display Brightness", fontSize = 18.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
    Text("Adjust in-car screen luminance to reduce glare while driving at night or in bright sun.", fontSize = 12.sp, color = Color.Gray)

    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Screen Luminance", fontWeight = FontWeight.Bold, color = Color.White)
                Text("${sliderValue.toInt()}%", fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
            }
            Slider(
                value = sliderValue,
                onValueChange = {
                    sliderValue = it
                    viewModel.systemStatusManager.setBrightness(it.toInt())
                },
                valueRange = 10f..100f,
                colors = SliderDefaults.colors(
                    thumbColor = MaterialTheme.colorScheme.primary,
                    activeTrackColor = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier.testTag("brightness_slider")
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(25 to "Night (25%)", 60 to "Balanced (60%)", 100 to "Max (100%)").forEach { (valPct, label) ->
                    Button(
                        onClick = {
                            sliderValue = valPct.toFloat()
                            viewModel.systemStatusManager.setBrightness(valPct)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }
        }
    }

    Button(
        onClick = { viewModel.appsManager.launchSystemSettings(Settings.ACTION_DISPLAY_SETTINGS) },
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.testTag("open_system_display_settings")
    ) {
        Icon(Icons.Default.Brightness6, contentDescription = null, tint = Color.Black, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text("Open Android System Display Settings", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
    }
}

@Composable
private fun DayNightSection(viewModel: CarHeadUnitViewModel, isNightMode: Boolean, isAutoNight: Boolean) {
    Text("Day / Night Mode", fontSize = 18.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
    Text("Select cockpit visual mode. Night mode utilizes pure OLED black to minimize cabin reflections.", fontSize = 12.sp, color = Color.Gray)

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = if (isNightMode) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
            border = BorderStroke(1.5.dp, if (isNightMode) MaterialTheme.colorScheme.primary else Color.Transparent),
            modifier = Modifier
                .weight(1f)
                .clickable { viewModel.setNightMode(true) }
                .padding(4.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.DarkMode, contentDescription = "Night", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
                Spacer(modifier = Modifier.height(6.dp))
                Text("NIGHT MODE", fontWeight = FontWeight.Black, fontSize = 13.sp, color = Color.White)
                Text("OLED Black surfaces", fontSize = 11.sp, color = Color.Gray)
            }
        }

        Surface(
            shape = RoundedCornerShape(12.dp),
            color = if (!isNightMode) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
            border = BorderStroke(1.5.dp, if (!isNightMode) MaterialTheme.colorScheme.primary else Color.Transparent),
            modifier = Modifier
                .weight(1f)
                .clickable { viewModel.setNightMode(false) }
                .padding(4.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.Brightness6, contentDescription = "Day", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
                Spacer(modifier = Modifier.height(6.dp))
                Text("DAY MODE", fontWeight = FontWeight.Black, fontSize = 13.sp, color = Color.White)
                Text("High ambient contrast", fontSize = 11.sp, color = Color.Gray)
            }
        }
    }

    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Automatic Day/Night Mode", fontWeight = FontWeight.Bold, color = Color.White)
                Text("Synchronizes with vehicle headlight sensor or sunset times", fontSize = 11.sp, color = Color.Gray)
            }
            Switch(
                checked = isAutoNight,
                onCheckedChange = { viewModel.setAutoNightMode(it) },
                colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.primary)
            )
        }
    }
}

@Composable
private fun WallpaperSection(viewModel: CarHeadUnitViewModel, currentWallpaper: CarWallpaper) {
    Text("Wallpaper & Cockpit Texture", fontSize = 18.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
    Text("Choose wallpaper theme optimized for automotive head unit displays.", fontSize = 12.sp, color = Color.Gray)

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        CarWallpaper.values().forEach { wp ->
            val isSelected = wp == currentWallpaper
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant,
                border = BorderStroke(1.dp, if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.setWallpaper(wp) }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp, 24.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(wp.endColor)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(wp.displayName, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                    }
                    if (isSelected) {
                        Icon(Icons.Default.Check, contentDescription = "Active", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }
}

@Composable
private fun DateTimeSection(viewModel: CarHeadUnitViewModel, currentTime: String, currentDate: String) {
    Text("Date & Time", fontSize = 18.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
    Text("System clock synchronized with GPS satellites.", fontSize = 12.sp, color = Color.Gray)

    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(currentTime, fontSize = 36.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace, color = MaterialTheme.colorScheme.primary)
            Text(currentDate, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
        }
    }

    Button(
        onClick = { viewModel.appsManager.launchSystemSettings(Settings.ACTION_DATE_SETTINGS) },
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
        shape = RoundedCornerShape(10.dp)
    ) {
        Icon(Icons.Default.AccessTime, contentDescription = null, tint = Color.Black, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text("Open Android Date & Time Settings", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
    }
}

@Composable
private fun BluetoothSection(viewModel: CarHeadUnitViewModel, isBtOn: Boolean, phoneName: String) {
    Text("Bluetooth Connection", fontSize = 18.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
    Text("Wireless phone audio and hands-free call integration with vehicle head unit.", fontSize = 12.sp, color = Color.Gray)

    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text("Connected Device", fontWeight = FontWeight.Bold, color = Color.White)
                Text(phoneName, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
            }
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = Color(0xFF00E676).copy(alpha = 0.2f),
                border = BorderStroke(1.dp, Color(0xFF00E676))
            ) {
                Text("CONNECTED", color = Color(0xFF00E676), fontWeight = FontWeight.Black, fontSize = 10.sp, modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp))
            }
        }
    }

    Button(
        onClick = { viewModel.appsManager.launchSystemSettings(Settings.ACTION_BLUETOOTH_SETTINGS) },
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
        shape = RoundedCornerShape(10.dp)
    ) {
        Icon(Icons.Default.Bluetooth, contentDescription = null, tint = Color.Black, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text("Open Android Bluetooth Settings", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
    }
}

@Composable
private fun WifiSection(viewModel: CarHeadUnitViewModel, isWifiConnected: Boolean, ssid: String) {
    Text("Wi-Fi & Network Hotspot", fontSize = 18.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
    Text("Connect head unit to car hotspot or mobile tethering for traffic updates.", fontSize = 12.sp, color = Color.Gray)

    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text("Current Network", fontWeight = FontWeight.Bold, color = Color.White)
                Text(ssid, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
            }
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = if (isWifiConnected) Color(0xFF00E676).copy(alpha = 0.2f) else Color.Gray.copy(alpha = 0.2f),
                border = BorderStroke(1.dp, if (isWifiConnected) Color(0xFF00E676) else Color.Gray)
            ) {
                Text(
                    if (isWifiConnected) "CONNECTED" else "DISCONNECTED",
                    color = if (isWifiConnected) Color(0xFF00E676) else Color.Gray,
                    fontWeight = FontWeight.Black,
                    fontSize = 10.sp,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                )
            }
        }
    }

    Button(
        onClick = { viewModel.appsManager.launchSystemSettings(Settings.ACTION_WIFI_SETTINGS) },
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
        shape = RoundedCornerShape(10.dp)
    ) {
        Icon(Icons.Default.Wifi, contentDescription = null, tint = Color.Black, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text("Open Android Wi-Fi Settings", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
    }
}

@Composable
private fun SoundSection(viewModel: CarHeadUnitViewModel, volumePercent: Int) {
    var volValue by remember(volumePercent) { mutableStateOf(volumePercent.toFloat()) }

    Text("Sound & Audio Mixing", fontSize = 18.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
    Text("Seamless background audio ducking during spoken navigation prompts and TTS voice alerts.", fontSize = 12.sp, color = Color.Gray)

    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Master Media Volume", fontWeight = FontWeight.Bold, color = Color.White)
                Text("${volValue.toInt()}%", fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
            }
            Slider(
                value = volValue,
                onValueChange = {
                    volValue = it
                    viewModel.systemStatusManager.setSystemVolume(it.toInt())
                },
                valueRange = 0f..100f,
                colors = SliderDefaults.colors(
                    thumbColor = MaterialTheme.colorScheme.primary,
                    activeTrackColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    }

    Button(
        onClick = { viewModel.appsManager.launchSystemSettings(Settings.ACTION_SOUND_SETTINGS) },
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
        shape = RoundedCornerShape(10.dp)
    ) {
        Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = null, tint = Color.Black, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text("Open Android System Sound Settings", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
    }
}

@Composable
private fun LauncherSection(viewModel: CarHeadUnitViewModel, isKmh: Boolean) {
    Text("Car Launcher Configuration", fontSize = 18.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
    Text("Configure home launcher behavior, speed units, and steering wheel integration.", fontSize = 12.sp, color = Color.Gray)

    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text("Default Home Launcher", fontWeight = FontWeight.Bold, color = Color.White)
                Text("Set this Car Launcher as Android's default home screen", fontSize = 11.sp, color = Color.Gray)
            }
            Button(
                onClick = { viewModel.appsManager.promptSetDefaultHome() },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Set Default", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp)
            }
        }
    }

    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text("Speedometer Units", fontWeight = FontWeight.Bold, color = Color.White)
                Text(if (isKmh) "Metric (km/h)" else "Imperial (mph)", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { viewModel.setSpeedUnit(true) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isKmh) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("KM/H", color = if (isKmh) Color.Black else Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }
                Button(
                    onClick = { viewModel.setSpeedUnit(false) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (!isKmh) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("MPH", color = if (!isKmh) Color.Black else Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }
            }
        }
    }

    // Boot StartUp for Radio Tape Recorders
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text("Automotive StartUp on Ignition / Boot", fontWeight = FontWeight.Bold, color = Color.White)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "CarLauncherBootReceiver is active in AndroidManifest.xml. The program launches automatically as soon as the radio tape recorder / head unit powers on.",
                fontSize = 11.sp,
                color = Color(0xFF00E676)
            )
        }
    }

    // Direct links to vehicle sub-screens
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Button(
            onClick = { viewModel.navigateTo(CarScreen.STEERING_WHEEL_CONFIG) },
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.weight(1f)
        ) {
            Text("Steering Wheel Controls", color = MaterialTheme.colorScheme.onSurface, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
        Button(
            onClick = { viewModel.navigateTo(CarScreen.DIAGNOSTICS) },
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.weight(1f)
        ) {
            Text("OBD Telemetry", color = MaterialTheme.colorScheme.onSurface, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun ThemesAndGammaSection(viewModel: CarHeadUnitViewModel, state: QuickAppsState) {
    Text("Principal Screen Themes", fontSize = 18.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
    Text("Select from 3 default subjects created specifically for Car Launcher:", fontSize = 12.sp, color = Color.Gray)

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        LauncherScreenTheme.values().forEach { theme ->
            val isSel = state.launcherTheme == theme
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (isSel) state.textColorGamma.color.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant,
                border = BorderStroke(1.dp, if (isSel) state.textColorGamma.color else MaterialTheme.colorScheme.outline),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.quickAppsManager.setLauncherTheme(theme) }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(theme.displayName, fontWeight = FontWeight.Black, color = Color.White, fontSize = 13.sp)
                        Text(theme.subtitle, fontSize = 11.sp, color = Color.Gray)
                    }
                    if (isSel) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = state.textColorGamma.color)
                    }
                }
            }
        }
    }

    Spacer(modifier = Modifier.height(10.dp))

    Text("Text Color Gamma", fontSize = 16.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
    Text("Change the color gamma of all instrument typography and cockpit accents:", fontSize = 12.sp, color = Color.Gray)

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        TextColorGamma.values().forEach { gamma ->
            val isSel = state.textColorGamma == gamma
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (isSel) gamma.color.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
                border = BorderStroke(if (isSel) 2.dp else 1.dp, if (isSel) gamma.color else Color.Transparent),
                modifier = Modifier
                    .weight(1f)
                    .clickable { viewModel.quickAppsManager.setTextColorGamma(gamma) }
            ) {
                Column(
                    modifier = Modifier.padding(vertical = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(modifier = Modifier.size(20.dp).clip(CircleShape).background(gamma.color))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(gamma.displayName.take(7), fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}

@Composable
private fun CarLogoSection(viewModel: CarHeadUnitViewModel, currentLogo: CarBrandLogo) {
    Text("Vehicle Brand Logo", fontSize = 18.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
    Text("Choose emblem badge to display on the digital instrument cluster, hub, and status bar:", fontSize = 12.sp, color = Color.Gray)

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        CarBrandLogo.values().toList().chunked(3).forEach { rowLogos ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                rowLogos.forEach { logo ->
                    val isSel = logo == currentLogo
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSel) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
                        border = BorderStroke(1.dp, if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { viewModel.quickAppsManager.setCarLogo(logo) }
                    ) {
                        Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(logo.shortTag, fontSize = 16.sp, fontWeight = FontWeight.Black, color = if (isSel) MaterialTheme.colorScheme.primary else Color.White)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(logo.displayName, fontSize = 10.sp, maxLines = 1, color = Color.Gray)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickAppsConfigSection(viewModel: CarHeadUnitViewModel, state: QuickAppsState) {
    Text("Quick Applications Settings", fontSize = 18.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
    Text("Configure app grid behavior, folders, curvature, and infinite scrolling:", fontSize = 12.sp, color = Color.Gray)

    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            // Infinite scrolling
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Infinite Scrolling", fontWeight = FontWeight.Bold, color = Color.White)
                    Text("Continuously scroll through selected applications", fontSize = 11.sp, color = Color.Gray)
                }
                Switch(
                    checked = state.infiniteScroll,
                    onCheckedChange = { viewModel.quickAppsManager.setInfiniteScroll(it) },
                    colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.primary)
                )
            }

            // Grid size
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Grid Count: ${state.gridColumns} apps", fontWeight = FontWeight.Bold, color = Color.White)
                    Text("Number of applications visible in a row", fontSize = 11.sp, color = Color.Gray)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(4, 5, 6, 8).forEach { count ->
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (state.gridColumns == count) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.08f),
                            modifier = Modifier.clickable { viewModel.quickAppsManager.setGridColumns(count) }
                        ) {
                            Text(
                                "$count",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (state.gridColumns == count) Color.Black else Color.White,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            // Bend side / Curvature
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Bend Side & Flex Angle", fontWeight = FontWeight.Bold, color = Color.White)
                    Text("Curve application icons along dashboard edge (${state.flexAngle.toInt()}°)", fontSize = 11.sp, color = Color.Gray)
                }
                Switch(
                    checked = state.bendSide,
                    onCheckedChange = { viewModel.quickAppsManager.setBendSide(it) },
                    colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.primary)
                )
            }

            if (state.bendSide) {
                Slider(
                    value = state.flexAngle,
                    onValueChange = { viewModel.quickAppsManager.setFlexAngle(it) },
                    valueRange = 0f..30f,
                    colors = SliderDefaults.colors(thumbColor = MaterialTheme.colorScheme.primary)
                )
            }
        }
    }
}

@Composable
private fun ScreenSaverSection(viewModel: CarHeadUnitViewModel) {
    Text("Clock Screen Saver", fontSize = 18.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
    Text("Opens when clicking for hours on top bar or center dashboard chronometer:", fontSize = 12.sp, color = Color.Gray)

    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Screen Saver Features:", fontWeight = FontWeight.Bold, color = Color.White)
            Text("• 4 Design Prototypes: Minimal Digital OLED, Classic Chrono, Retro Flip Clock, Ambient Night HUD", fontSize = 11.sp, color = Color.LightGray)
            Text("• Multiple Fonts: Digital Monospace, Modern Sans, Luxury Serif", fontSize = 11.sp, color = Color.LightGray)
            Text("• Multiple Date Formats with custom element sizing and color presets", fontSize = 11.sp, color = Color.LightGray)
            Text("• Data Movement Burn-in Protection: Gentle drifting animation to safeguard OLED/LCD head unit displays", fontSize = 11.sp, color = Color(0xFF00E676))
            Text("• Anti-Glare Brightness Reduction when hours are open", fontSize = 11.sp, color = Color.LightGray)

            Spacer(modifier = Modifier.height(4.dp))

            Button(
                onClick = { viewModel.setClockScreenSaverOpen(true) },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.AvTimer, contentDescription = null, tint = Color.Black, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("TEST / LAUNCH SCREEN SAVER NOW", color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Black)
            }
        }
    }
}

@Composable
private fun AboutSection(viewModel: CarHeadUnitViewModel) {
    Text("About Car Launcher", fontSize = 18.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)

    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Car Launcher — Automotive Head Unit Edition", fontWeight = FontWeight.Black, fontSize = 14.sp, color = Color.White)
            Text("Target Screen: 9-inch 1280 × 720 px (16:9 Landscape)", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
            Text("Target OS: Android 9.0 (API 28) & modern Android head units", fontSize = 12.sp, color = Color.LightGray)
            Text("Vehicle Profile: Maruti Suzuki Celerio K10C DualJet Engine", fontSize = 12.sp, color = Color.LightGray)
            Text("Features: CAN-bus Steering Controls, Offline Map Engine, Real-time Speed Monitoring, Audio Mixing & Background Ducking, OBD-II Diagnostics, Emergency SOS 112.", fontSize = 11.sp, color = Color.Gray)
        }
    }
}
