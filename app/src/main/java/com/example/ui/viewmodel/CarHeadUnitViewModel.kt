package com.example.ui.viewmodel

import android.app.Application
import android.content.Intent
import android.net.Uri
import android.view.KeyEvent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.CarAudioPlayer
import com.example.audio.VoiceTtsManager
import com.example.data.database.CarHeadUnitDatabase
import com.example.data.entity.DtcCodeEntity
import com.example.data.entity.TripLogEntity
import com.example.data.repository.CarRepository
import com.example.hardware.GpsSpeedManager
import com.example.hardware.ObdDiagnosticsManager
import com.example.hardware.SteeringWheelAction
import com.example.hardware.SteeringWheelEvent
import com.example.hardware.SteeringWheelManager
import com.example.hardware.VoiceCommandManager
import com.example.hardware.VoiceCommandResult
import com.example.navigation.DestinationPreset
import com.example.navigation.OfflineMapCacheManager
import com.example.navigation.OfflineNavigationEngine
import com.example.hardware.OnBoardComputerManager
import com.example.launcher.AppFolder
import com.example.launcher.AppInfo
import com.example.launcher.AppsManager
import com.example.launcher.QuickAppsManager
import com.example.launcher.SystemStatusManager
import com.example.ui.theme.CarWallpaper
import com.example.ui.theme.CockpitTheme
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class CarScreen {
    DASHBOARD,
    SPLIT_VIEW,
    NAVIGATION,
    FM_RADIO,
    MUSIC,
    PHONE,
    APPS,
    SETTINGS,
    DIAGNOSTICS,
    TRIP_LOG,
    STEERING_WHEEL_CONFIG
}

data class ActiveCallState(
    val isInCall: Boolean = false,
    val isIncoming: Boolean = false,
    val callerName: String = "",
    val callerNumber: String = "",
    val durationSeconds: Int = 0,
    val isMuted: Boolean = false,
    val isSpeakerOn: Boolean = true
)

data class HeadUnitUiState(
    val currentScreen: CarScreen = CarScreen.DASHBOARD,
    val isNightMode: Boolean = true,
    val isAutoNightMode: Boolean = false,
    val cockpitTheme: CockpitTheme = CockpitTheme.STANDARD,
    val wallpaper: CarWallpaper = CarWallpaper.DARK_CARBON,
    val isEmergencySosVisible: Boolean = false,
    val toastMessage: String? = null,
    val lastVoiceCommand: String? = null,
    val lastSteeringAction: SteeringWheelAction? = null,
    val speedUnitKmh: Boolean = true,
    val dialedNumber: String = "",
    val isOnBoardComputerOpen: Boolean = false,
    val isClockScreenSaverOpen: Boolean = false,
    val selectedActionApp: AppInfo? = null,
    val activeFolder: AppFolder? = null
)

class CarHeadUnitViewModel(application: Application) : AndroidViewModel(application) {

    private val database = CarHeadUnitDatabase.getDatabase(application, viewModelScope)
    val repository = CarRepository(
        tripDao = database.tripDao(),
        dtcDao = database.dtcDao(),
        maintenanceDao = database.maintenanceDao()
    )

    // Launcher & System status
    val appsManager = AppsManager(application)
    val quickAppsManager = QuickAppsManager(application)
    val onBoardComputer = OnBoardComputerManager(viewModelScope)
    val systemStatusManager = SystemStatusManager(application, viewModelScope)

    // Audio & TTS
    val audioPlayer = CarAudioPlayer(application, viewModelScope)
    val ttsManager = VoiceTtsManager(
        context = application,
        onSpeechStarted = {
            audioPlayer.duckVolume(true)
        },
        onSpeechFinished = {
            audioPlayer.duckVolume(false)
        }
    )

    // Navigation & Map Caching
    val navEngine = OfflineNavigationEngine(viewModelScope, ttsManager)
    val mapCacheManager = OfflineMapCacheManager(viewModelScope)

    // Hardware Managers
    val steeringWheelManager = SteeringWheelManager()
    val obdManager = ObdDiagnosticsManager(viewModelScope)
    val gpsSpeedManager = GpsSpeedManager(application) { speedKmh ->
        navEngine.updateCurrentSpeed(speedKmh)
        onBoardComputer.onSpeedUpdate(speedKmh.toFloat())
    }

    // Voice Assistant
    val voiceCommandManager = VoiceCommandManager(application) { result ->
        handleVoiceCommandResult(result)
    }

    // UI State
    private val _uiState = MutableStateFlow(HeadUnitUiState())
    val uiState: StateFlow<HeadUnitUiState> = _uiState.asStateFlow()

    private val _activeCall = MutableStateFlow(ActiveCallState())
    val activeCall: StateFlow<ActiveCallState> = _activeCall.asStateFlow()

    private var callJob: kotlinx.coroutines.Job? = null

    // Data streams from Room
    val allTrips = repository.allTrips.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val activeDtcCodes = repository.activeDtcCodes.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val maintenanceAlerts = repository.maintenanceAlerts.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val totalDistance = repository.totalDistance.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 170.9f)
    val averageFuelEfficiency = repository.averageFuelEfficiency.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 22.55f)

    init {
        // Preload installed applications
        viewModelScope.launch {
            appsManager.loadInstalledApps()
        }

        // Collect steering wheel hardware button presses
        viewModelScope.launch {
            steeringWheelManager.lastEvent.collect { event ->
                handleSteeringWheelEvent(event)
            }
        }

        // Start GPS updates
        gpsSpeedManager.startGpsUpdates()
    }

    fun navigateTo(screen: CarScreen) {
        _uiState.value = _uiState.value.copy(currentScreen = screen)
    }

    fun toggleNightMode() {
        val newNight = !_uiState.value.isNightMode
        _uiState.value = _uiState.value.copy(isNightMode = newNight)
        showNotification(if (newNight) "Night Mode (High Contrast Cockpit)" else "Day Mode")
    }

    fun setNightMode(isNight: Boolean) {
        _uiState.value = _uiState.value.copy(isNightMode = isNight)
    }

    fun setAutoNightMode(auto: Boolean) {
        _uiState.value = _uiState.value.copy(isAutoNightMode = auto)
        showNotification(if (auto) "Auto Day/Night Mode Enabled" else "Manual Day/Night Mode")
    }

    fun setWallpaper(wallpaper: CarWallpaper) {
        _uiState.value = _uiState.value.copy(wallpaper = wallpaper)
        showNotification("Wallpaper: ${wallpaper.displayName}")
    }

    fun setSpeedUnit(isKmh: Boolean) {
        _uiState.value = _uiState.value.copy(speedUnitKmh = isKmh)
        showNotification(if (isKmh) "Speed Unit: KM/H" else "Speed Unit: MPH")
    }

    // Phone / Dialer actions
    fun updateDialedNumber(number: String) {
        _uiState.value = _uiState.value.copy(dialedNumber = number)
    }

    fun appendDialDigit(digit: String) {
        val current = _uiState.value.dialedNumber
        if (current.length < 15) {
            _uiState.value = _uiState.value.copy(dialedNumber = current + digit)
        }
    }

    fun deleteDialDigit() {
        val current = _uiState.value.dialedNumber
        if (current.isNotEmpty()) {
            _uiState.value = _uiState.value.copy(dialedNumber = current.dropLast(1))
        }
    }

    fun clearDialDigits() {
        _uiState.value = _uiState.value.copy(dialedNumber = "")
    }

    fun startCall(number: String, name: String = "") {
        val cleanNumber = number.ifBlank { _uiState.value.dialedNumber }
        if (cleanNumber.isBlank()) {
            appsManager.launchDialer()
            return
        }
        callJob?.cancel()
        _activeCall.value = ActiveCallState(
            isInCall = true,
            isIncoming = false,
            callerName = if (name.isNotBlank()) name else cleanNumber,
            callerNumber = cleanNumber,
            durationSeconds = 0,
            isMuted = false,
            isSpeakerOn = true
        )
        audioPlayer.duckVolume(true)
        val displayName = if (name.isNotBlank()) name else cleanNumber
        showNotification("Dialing $displayName...")
        appsManager.makePhoneCall(cleanNumber)

        callJob = viewModelScope.launch {
            while (_activeCall.value.isInCall) {
                kotlinx.coroutines.delay(1000L)
                _activeCall.value = _activeCall.value.copy(
                    durationSeconds = _activeCall.value.durationSeconds + 1
                )
            }
        }
    }

    fun answerCall() {
        val current = _activeCall.value
        _activeCall.value = current.copy(isIncoming = false, isInCall = true, durationSeconds = 0)
        showNotification("Call Connected: ${current.callerName}")
        ttsManager.speak("Call connected.", isPriority = true)
        callJob?.cancel()
        callJob = viewModelScope.launch {
            while (_activeCall.value.isInCall) {
                kotlinx.coroutines.delay(1000L)
                _activeCall.value = _activeCall.value.copy(
                    durationSeconds = _activeCall.value.durationSeconds + 1
                )
            }
        }
    }

    fun endCall() {
        callJob?.cancel()
        val prevName = _activeCall.value.callerName
        _activeCall.value = ActiveCallState()
        audioPlayer.duckVolume(false)
        showNotification("Call Ended: $prevName")
        ttsManager.speak("Call ended.")
    }

    fun toggleCallMute() {
        val current = _activeCall.value.isMuted
        _activeCall.value = _activeCall.value.copy(isMuted = !current)
        showNotification(if (!current) "Microphone Muted" else "Microphone Unmuted")
    }

    fun toggleCallSpeaker() {
        val current = _activeCall.value.isSpeakerOn
        _activeCall.value = _activeCall.value.copy(isSpeakerOn = !current)
        showNotification(if (!current) "Speakerphone ON" else "Handset Mode")
    }

    fun simulateIncomingCall(number: String = "+91 98765 43210", name: String = "Rahul Sharma") {
        _activeCall.value = ActiveCallState(
            isInCall = false,
            isIncoming = true,
            callerName = name,
            callerNumber = number
        )
        audioPlayer.duckVolume(true)
        ttsManager.speak("Incoming call from $name.", isPriority = true)
    }

    fun callDialedNumber() {
        val number = _uiState.value.dialedNumber
        if (number.isNotBlank()) {
            startCall(number)
        } else {
            appsManager.launchDialer()
        }
    }

    fun launchYouTubeApp(query: String? = null) {
        try {
            val uri = if (query != null) {
                Uri.parse("https://www.youtube.com/results?search_query=${Uri.encode(query)}")
            } else {
                Uri.parse("https://www.youtube.com")
            }
            val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                setPackage("com.google.android.youtube")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            getApplication<Application>().startActivity(intent)
        } catch (_: Exception) {
            val webUri = if (query != null) {
                Uri.parse("https://m.youtube.com/results?search_query=${Uri.encode(query)}")
            } else {
                Uri.parse("https://m.youtube.com")
            }
            val webIntent = Intent(Intent.ACTION_VIEW, webUri).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            try {
                getApplication<Application>().startActivity(webIntent)
            } catch (_: Exception) {
                showNotification("Opening YouTube...")
            }
        }
    }

    fun setCockpitTheme(theme: CockpitTheme) {
        _uiState.value = _uiState.value.copy(cockpitTheme = theme)
        showNotification("Theme set: ${theme.displayName}")
    }

    fun setOnBoardComputerOpen(open: Boolean) {
        _uiState.value = _uiState.value.copy(isOnBoardComputerOpen = open)
    }

    fun setClockScreenSaverOpen(open: Boolean) {
        _uiState.value = _uiState.value.copy(isClockScreenSaverOpen = open)
    }

    fun setSelectedActionApp(app: AppInfo?) {
        _uiState.value = _uiState.value.copy(selectedActionApp = app)
    }

    fun setActiveFolder(folder: AppFolder?) {
        _uiState.value = _uiState.value.copy(activeFolder = folder)
    }

    fun toggleSpeedUnit() {
        val current = _uiState.value.speedUnitKmh
        _uiState.value = _uiState.value.copy(speedUnitKmh = !current)
        showNotification("Speed unit: ${if (!current) "KM/H" else "MPH"}")
    }

    fun showEmergencySos(show: Boolean) {
        _uiState.value = _uiState.value.copy(isEmergencySosVisible = show)
        if (show) {
            ttsManager.speak("Emergency assistance screen opened. GPS coordinates locked.", isPriority = true)
        }
    }

    fun showNotification(msg: String) {
        _uiState.value = _uiState.value.copy(toastMessage = msg)
        viewModelScope.launch {
            kotlinx.coroutines.delay(2800L)
            if (_uiState.value.toastMessage == msg) {
                _uiState.value = _uiState.value.copy(toastMessage = null)
            }
        }
    }

    fun handleSteeringWheelEvent(event: SteeringWheelEvent) {
        _uiState.value = _uiState.value.copy(lastSteeringAction = event.action)
        showNotification("Steering Control: ${event.action.label}")

        when (event.action) {
            SteeringWheelAction.PLAY_PAUSE -> audioPlayer.togglePlayPause()
            SteeringWheelAction.NEXT_TRACK -> audioPlayer.nextTrack()
            SteeringWheelAction.PREV_TRACK -> audioPlayer.previousTrack()
            SteeringWheelAction.VOLUME_UP -> showNotification("Volume Up")
            SteeringWheelAction.VOLUME_DOWN -> showNotification("Volume Down")
            SteeringWheelAction.MUTE -> {
                audioPlayer.pause()
                showNotification("Audio Muted")
            }
            SteeringWheelAction.VOICE_COMMAND -> {
                triggerVoiceCommand()
            }
            SteeringWheelAction.TOGGLE_HUD -> {
                navEngine.toggleHudMode()
            }
            SteeringWheelAction.EMERGENCY_SOS -> {
                showEmergencySos(true)
            }
        }
    }

    fun onHardwareKeyEvent(keyCode: Int, event: KeyEvent): Boolean {
        return steeringWheelManager.onKeyEvent(keyCode, event)
    }

    fun triggerVoiceCommand() {
        voiceCommandManager.startListening()
        ttsManager.speak("Listening for car command...")
    }

    fun submitVoiceQuery(query: String) {
        val result = voiceCommandManager.processCommand(query)
        handleVoiceCommandResult(result)
    }

    private fun handleVoiceCommandResult(result: VoiceCommandResult) {
        when (result) {
            is VoiceCommandResult.Success -> {
                _uiState.value = _uiState.value.copy(lastVoiceCommand = result.message)
                showNotification(result.message)
                ttsManager.speak(result.message)

                when (result.action) {
                    "PLAY" -> audioPlayer.play()
                    "PAUSE" -> audioPlayer.pause()
                    "NEXT_TRACK" -> audioPlayer.nextTrack()
                    "PREV_TRACK" -> audioPlayer.previousTrack()
                    "NAV_HOME" -> {
                        navEngine.presetDestinations.firstOrNull { it.category == "Home" }?.let {
                            navEngine.startNavigation(it)
                            navigateTo(CarScreen.NAVIGATION)
                        }
                    }
                    "NAV_HIGHWAY" -> {
                        navEngine.presetDestinations.firstOrNull { it.category == "Highway" }?.let {
                            navEngine.startNavigation(it)
                            navigateTo(CarScreen.NAVIGATION)
                        }
                    }
                    "NAV_FUEL" -> {
                        navEngine.presetDestinations.firstOrNull { it.category == "Fuel" }?.let {
                            navEngine.startNavigation(it)
                            navigateTo(CarScreen.NAVIGATION)
                        }
                    }
                    "NAV_SERVICE" -> {
                        navEngine.presetDestinations.firstOrNull { it.category == "Service" }?.let {
                            navEngine.startNavigation(it)
                            navigateTo(CarScreen.NAVIGATION)
                        }
                    }
                    "STOP_NAV" -> navEngine.stopNavigation()
                    "TOGGLE_HUD" -> navEngine.toggleHudMode()
                    "THEME_NIGHT" -> setNightMode(true)
                    "THEME_DAY" -> setNightMode(false)
                    "DIAGNOSTICS" -> navigateTo(CarScreen.DIAGNOSTICS)
                    "SOS" -> showEmergencySos(true)
                    "FM_RADIO" -> navigateTo(CarScreen.FM_RADIO)
                    "MUTE" -> audioPlayer.pause()
                }
            }
            is VoiceCommandResult.Unrecognized -> {
                val feedback = "Command not recognized: '${result.query}'. Try 'navigate home' or 'play music'."
                _uiState.value = _uiState.value.copy(lastVoiceCommand = feedback)
                showNotification(feedback)
                ttsManager.speak(feedback)
            }
        }
    }

    fun recordCompletedTrip() {
        viewModelScope.launch {
            val nav = navEngine.navState.value
            val dist = if (nav.isNavigating) nav.distanceRemainingKm.coerceAtLeast(12.5f) else 18.2f
            val fuelEfficiency = 23.8f // Celerio DualJet K10C
            val fuelUsed = (dist / fuelEfficiency)
            val cost = fuelUsed * 96f // Rs 96 per Liter

            val newTrip = TripLogEntity(
                title = nav.activeDestination?.name ?: "Highway & City Cruise",
                distanceKm = dist,
                durationMinutes = 24,
                avgSpeedKmh = 58.2f,
                topSpeedKmh = 86.0f,
                fuelConsumedLiters = (fuelUsed * 10).toInt() / 10f,
                fuelEfficiencyKmpl = fuelEfficiency,
                estimatedCostInr = (cost * 10).toInt() / 10f,
                startLocationName = "Sector 29, Gurgaon",
                destinationName = nav.activeDestination?.name ?: "NH48 Toll Plaza",
                timestamp = System.currentTimeMillis()
            )
            repository.saveTrip(newTrip)
            showNotification("Trip Logged: ${dist} km at ${fuelEfficiency} km/L")
            ttsManager.speak("Trip logged successfully. Fuel efficiency was ${fuelEfficiency} kilometers per liter.")
        }
    }

    fun dialEmergencyCall(number: String = "112") {
        try {
            val intent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:$number")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            getApplication<Application>().startActivity(intent)
        } catch (_: Exception) {
            showNotification("Dialing $number...")
        }
    }

    fun scanObdEcu() {
        ttsManager.speak("Initiating OBD-II ECU scan for Maruti Suzuki Celerio.")
        obdManager.triggerScan { success ->
            if (success) {
                ttsManager.speak("Diagnostic scan complete. All engine sensors operating normally.")
                showNotification("OBD-II Scan Complete: Engine Normal")
            }
        }
    }

    fun clearDtcErrors() {
        viewModelScope.launch {
            repository.clearAllDtcCodes()
            obdManager.clearCodes()
            ttsManager.speak("Diagnostic trouble codes cleared from ECU memory.")
            showNotification("ECU Trouble Codes Cleared")
        }
    }

    override fun onCleared() {
        super.onCleared()
        audioPlayer.release()
        ttsManager.release()
        voiceCommandManager.release()
        gpsSpeedManager.stopGpsUpdates()
        obdManager.stop()
    }
}
