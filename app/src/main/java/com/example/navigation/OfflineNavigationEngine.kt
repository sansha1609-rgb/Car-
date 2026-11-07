package com.example.navigation

import com.example.audio.VoiceTtsManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class OfflineNavigationEngine(
    private val scope: CoroutineScope,
    private val ttsManager: VoiceTtsManager
) {
    val presetDestinations = listOf(
        DestinationPreset(
            id = "dest_home",
            name = "Home Residence",
            address = "Sector 56, Golf Course Road",
            distanceKm = 14.8f,
            etaMinutes = 22,
            speedLimitKmh = 50,
            category = "Home",
            coordinates = GeoPoint(28.4231, 77.0982)
        ),
        DestinationPreset(
            id = "dest_nh48",
            name = "NH48 Jaipur Super Highway",
            address = "Kherki Daula Toll Express Corridor",
            distanceKm = 48.2f,
            etaMinutes = 42,
            speedLimitKmh = 90,
            category = "Highway",
            coordinates = GeoPoint(28.3842, 76.9943)
        ),
        DestinationPreset(
            id = "dest_maruti_service",
            name = "Maruti Suzuki Arena Service",
            address = "Authorized Celerio Care Hub, Old Delhi Rd",
            distanceKm = 6.4f,
            etaMinutes = 14,
            speedLimitKmh = 40,
            category = "Service",
            coordinates = GeoPoint(28.4721, 77.0421)
        ),
        DestinationPreset(
            id = "dest_fuel_station",
            name = "Indian Oil Petrol & CNG Station",
            address = "24x7 Fuel & Air Pressure Point",
            distanceKm = 2.1f,
            etaMinutes = 5,
            speedLimitKmh = 40,
            category = "Fuel",
            coordinates = GeoPoint(28.4610, 77.0310)
        )
    )

    private val _navState = MutableStateFlow(
        NavigationState(
            isNavigating = false,
            currentSpeedKmh = 48,
            speedLimitKmh = 60,
            currentRoad = "MG Road Expressway Link"
        )
    )
    val navState: StateFlow<NavigationState> = _navState.asStateFlow()

    private var simulationJob: Job? = null
    private var maneuverIndex = 0

    private val sampleManeuvers = listOf(
        NavManeuver(
            type = ManeuverType.START,
            instruction = "Head northwest on Link Road toward Main Flyover",
            distanceRemainingMeters = 400,
            roadName = "Link Road",
            voicePrompt = "Starting route guidance. Head northwest on Link Road for 400 meters."
        ),
        NavManeuver(
            type = ManeuverType.TURN_RIGHT,
            instruction = "Turn right onto NH-48 Express Corridor",
            distanceRemainingMeters = 850,
            roadName = "NH-48 Corridor",
            voicePrompt = "In 800 meters, turn right onto National Highway 48."
        ),
        NavManeuver(
            type = ManeuverType.STRAIGHT,
            instruction = "Continue straight for 12 kilometers",
            distanceRemainingMeters = 12000,
            roadName = "NH-48 Express Corridor",
            voicePrompt = "Continue straight on National Highway 48 for 12 kilometers. Speed limit is 90."
        ),
        NavManeuver(
            type = ManeuverType.SLIGHT_LEFT,
            instruction = "Take exit ramp toward Service Road",
            distanceRemainingMeters = 600,
            roadName = "Service Road Exit",
            voicePrompt = "In 600 meters, take the slight left ramp onto the service road."
        ),
        NavManeuver(
            type = ManeuverType.ARRIVE,
            instruction = "Destination is ahead on your left",
            distanceRemainingMeters = 100,
            roadName = "Destination Arrival",
            voicePrompt = "You have arrived at your destination on the left."
        )
    )

    private val simulatedIncidents = listOf(
        TrafficIncident(
            id = "inc_1",
            title = "Speed Radar Trap",
            description = "Automated high-speed camera active on overhead gantry",
            distanceAheadMeters = 1200,
            type = IncidentType.SPEED_CAMERA,
            severity = "ALERT"
        ),
        TrafficIncident(
            id = "inc_2",
            title = "Lane Maintenance Ahead",
            description = "Right lane closed for resurfacing work",
            distanceAheadMeters = 3400,
            type = IncidentType.CONSTRUCTION,
            severity = "MODERATE"
        )
    )

    fun startNavigation(destination: DestinationPreset) {
        stopNavigation()
        // Route guidance disabled per user requirement - update live GPS location only
        _navState.value = _navState.value.copy(
            isNavigating = false,
            activeDestination = null,
            currentManeuver = null,
            currentRoad = destination.name,
            distanceRemainingKm = 0f,
            etaMinutes = 0
        )
    }

    fun stopNavigation() {
        simulationJob?.cancel()
        _navState.value = _navState.value.copy(
            isNavigating = false,
            activeDestination = null,
            currentManeuver = null,
            distanceRemainingKm = 0f,
            etaMinutes = 0,
            incidentsAhead = emptyList()
        )
        ttsManager.speak("Navigation stopped.")
    }

    fun toggleHudMode() {
        val newHud = !_navState.value.isHudMode
        _navState.value = _navState.value.copy(isHudMode = newHud)
        if (newHud) {
            ttsManager.speak("Heads up display mode activated. Place phone under windshield.")
        }
    }

    fun setHudMode(enabled: Boolean) {
        _navState.value = _navState.value.copy(isHudMode = enabled)
    }

    fun updateCurrentSpeed(speedKmh: Int) {
        val speedLimit = _navState.value.speedLimitKmh
        val isOver = speedKmh > speedLimit
        val overshoot = if (isOver) speedKmh - speedLimit else 0

        val wasOver = _navState.value.isOverSpeed
        _navState.value = _navState.value.copy(
            currentSpeedKmh = speedKmh,
            isOverSpeed = isOver,
            speedLimitOvershoot = overshoot
        )

        if (isOver && !wasOver && speedKmh > speedLimit + 5) {
            ttsManager.speak("Speed limit warning: Current speed is $speedKmh. Speed limit is $speedLimit.", isPriority = true)
        }
    }

    fun setSpeedLimit(limitKmh: Int) {
        _navState.value = _navState.value.copy(
            speedLimitKmh = limitKmh
        )
        updateCurrentSpeed(_navState.value.currentSpeedKmh)
    }

    private fun startRouteSimulation() {
        simulationJob = scope.launch(Dispatchers.Default) {
            var step = 0
            while (_navState.value.isNavigating) {
                delay(3500L)
                step++
                val currentState = _navState.value

                // Progress distance
                val newDist = (currentState.distanceRemainingKm - 0.4f).coerceAtLeast(0f)
                val newEta = (newDist / (currentState.currentSpeedKmh.coerceAtLeast(20) / 60f)).toInt()

                // Check maneuver advancement
                if (step % 4 == 0 && maneuverIndex < sampleManeuvers.size - 1) {
                    maneuverIndex++
                    val nextManeuver = sampleManeuvers[maneuverIndex]
                    _navState.value = _navState.value.copy(
                        currentManeuver = nextManeuver,
                        currentRoad = nextManeuver.roadName
                    )
                    ttsManager.speak(nextManeuver.voicePrompt, isPriority = true)
                }

                _navState.value = _navState.value.copy(
                    distanceRemainingKm = (newDist * 10).toInt() / 10f,
                    etaMinutes = newEta
                )

                if (newDist <= 0.05f) {
                    ttsManager.speak("You have reached your destination. Navigation ended.", isPriority = true)
                    stopNavigation()
                    break
                }
            }
        }
    }
}
