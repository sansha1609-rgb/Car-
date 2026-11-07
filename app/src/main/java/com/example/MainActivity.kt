package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.SettingsInputComponent
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.ui.components.CarBottomDock
import com.example.ui.components.HudDisplay
import com.example.ui.screens.AppsScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.DiagnosticsScreen
import com.example.ui.screens.EmergencySosDialog
import com.example.ui.screens.FmRadioScreen
import com.example.ui.screens.MusicScreen
import com.example.ui.screens.NavigationScreen
import com.example.ui.screens.PhoneScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SplitScreenView
import com.example.ui.screens.SteeringWheelConfigScreen
import com.example.ui.screens.TripLogScreen
import com.example.ui.theme.CelerioAutoTheme
import com.example.ui.theme.SpeedAlertRed
import com.example.ui.viewmodel.CarHeadUnitViewModel
import com.example.ui.viewmodel.CarScreen

class MainActivity : ComponentActivity() {

    private val viewModel: CarHeadUnitViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val insetsController = WindowCompat.getInsetsController(window, window.decorView)
        insetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        insetsController.hide(WindowInsetsCompat.Type.statusBars())

        setContent {
            val uiState by viewModel.uiState.collectAsState()
            val navState by viewModel.navEngine.navState.collectAsState()

            // Request car head unit permissions for real GPS and microphone
            val permissionsToRequest = mutableListOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.RECORD_AUDIO
            ).apply {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    add(Manifest.permission.READ_MEDIA_AUDIO)
                } else {
                    add(Manifest.permission.READ_EXTERNAL_STORAGE)
                }
            }

            val permissionLauncher = rememberLauncherForActivityResult(
                ActivityResultContracts.RequestMultiplePermissions()
            ) { /* Permissions handled */ }

            LaunchedEffect(Unit) {
                val needed = permissionsToRequest.filter {
                    ContextCompat.checkSelfPermission(this@MainActivity, it) != PackageManager.PERMISSION_GRANTED
                }
                if (needed.isNotEmpty()) {
                    permissionLauncher.launch(needed.toTypedArray())
                }
            }

            CelerioAutoTheme(
                isNightMode = uiState.isNightMode,
                cockpitTheme = uiState.cockpitTheme
            ) {
                // If Heads-Up Display mode is active, display pure windshield projection
                if (navState.isHudMode) {
                    HudDisplay(
                        navState = navState,
                        onExitHud = { viewModel.navEngine.setHudMode(false) }
                    )
                } else {
                    MainCarDashboardContent(
                        viewModel = viewModel,
                        currentScreen = uiState.currentScreen,
                        onNavigate = { viewModel.navigateTo(it) }
                    )
                }

                // Emergency SOS Dialog Modal
                if (uiState.isEmergencySosVisible) {
                    EmergencySosDialog(
                        viewModel = viewModel,
                        onDismiss = { viewModel.showEmergencySos(false) }
                    )
                }
            }
        }
    }

    /**
     * Intercepts steering wheel controls from car head unit (CAN-bus / resistive key events).
     */
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (event != null && viewModel.onHardwareKeyEvent(keyCode, event)) {
            return true
        }
        return super.onKeyDown(keyCode, event)
    }
}

@Composable
fun MainCarDashboardContent(
    viewModel: CarHeadUnitViewModel,
    currentScreen: CarScreen,
    onNavigate: (CarScreen) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val isVoiceListening by viewModel.voiceCommandManager.isListening.collectAsState()
    var showVoiceInputDialog by remember { mutableStateOf(false) }
    var voiceTextQuery by remember { mutableStateOf("") }

    BackHandler(enabled = uiState.isOnBoardComputerOpen || uiState.isClockScreenSaverOpen || currentScreen != CarScreen.DASHBOARD) {
        if (uiState.isClockScreenSaverOpen) {
            viewModel.setClockScreenSaverOpen(false)
        } else if (uiState.isOnBoardComputerOpen) {
            viewModel.setOnBoardComputerOpen(false)
        } else {
            onNavigate(CarScreen.DASHBOARD)
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding(),
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            CarBottomDock(
                currentScreen = currentScreen,
                onNavigate = onNavigate
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            uiState.wallpaper.startColor,
                            uiState.wallpaper.endColor
                        )
                    )
                )
                .padding(innerPadding)
        ) {
            // Main Screen Routing
            when (currentScreen) {
                CarScreen.DASHBOARD -> DashboardScreen(viewModel = viewModel)
                CarScreen.SPLIT_VIEW -> SplitScreenView(viewModel = viewModel)
                CarScreen.NAVIGATION -> NavigationScreen(viewModel = viewModel)
                CarScreen.FM_RADIO -> FmRadioScreen(viewModel = viewModel)
                CarScreen.MUSIC -> MusicScreen(viewModel = viewModel)
                CarScreen.PHONE -> PhoneScreen(viewModel = viewModel)
                CarScreen.APPS -> AppsScreen(viewModel = viewModel)
                CarScreen.SETTINGS -> SettingsScreen(viewModel = viewModel)
                CarScreen.DIAGNOSTICS -> DiagnosticsScreen(viewModel = viewModel)
                CarScreen.TRIP_LOG -> TripLogScreen(viewModel = viewModel)
                CarScreen.STEERING_WHEEL_CONFIG -> SteeringWheelConfigScreen(viewModel = viewModel)
            }

            // Notification / Steering Control Feedback Toast Overlay
            AnimatedVisibility(
                visible = uiState.toastMessage != null,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 12.dp)
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(24.dp),
                    shadowElevation = 8.dp,
                    modifier = Modifier.testTag("car_feedback_toast")
                ) {
                    Text(
                        text = uiState.toastMessage ?: "",
                        color = Color.Black,
                        fontWeight = FontWeight.Black,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(horizontal = 18.dp, vertical = 8.dp)
                    )
                }
            }

            // Hands-Free Voice Assistant Interactive Banner (Driver can tap to speak or type)
            if (isVoiceListening || showVoiceInputDialog) {
                Surface(
                    color = Color.Black.copy(alpha = 0.92f),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .align(Alignment.BottomCenter)
                        .testTag("voice_command_dialog")
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Mic,
                                    contentDescription = "Voice",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (isVoiceListening) "LISTENING TO VOICE COMMAND..." else "ENTER VOICE COMMAND",
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 12.sp,
                                    letterSpacing = 1.sp
                                )
                            }
                            IconButton(onClick = {
                                viewModel.voiceCommandManager.stopListening()
                                showVoiceInputDialog = false
                            }) {
                                Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Voice Command Suggestions
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf("Navigate Home", "Play Music", "Night Mode", "Diagnostics").forEach { cmd ->
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier
                                        .clickable {
                                            viewModel.submitVoiceQuery(cmd)
                                            showVoiceInputDialog = false
                                        }
                                ) {
                                    Text(
                                        text = cmd,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = voiceTextQuery,
                                onValueChange = { voiceTextQuery = it },
                                placeholder = { Text("e.g. 'find fuel' or 'pause music'", color = Color.Gray, fontSize = 12.sp) },
                                singleLine = true,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("voice_command_input"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .size(48.dp)
                                    .clickable {
                                        if (voiceTextQuery.isNotBlank()) {
                                            viewModel.submitVoiceQuery(voiceTextQuery)
                                            voiceTextQuery = ""
                                            showVoiceInputDialog = false
                                        }
                                    }
                                    .testTag("send_voice_command_button")
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.Send, contentDescription = "Send", tint = Color.Black)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

