package com.example.navigation

enum class ManeuverType {
    START,
    STRAIGHT,
    TURN_LEFT,
    TURN_RIGHT,
    SLIGHT_LEFT,
    SLIGHT_RIGHT,
    ROUNDABOUT_ENTER,
    ROUNDABOUT_EXIT,
    U_TURN,
    ARRIVE
}

enum class IncidentType {
    ACCIDENT,
    CONSTRUCTION,
    SPEED_CAMERA,
    HEAVY_CONGESTION,
    WEATHER_HAZARD
}

data class GeoPoint(
    val latitude: Double,
    val longitude: Double
)

data class NavManeuver(
    val type: ManeuverType,
    val instruction: String,
    val distanceRemainingMeters: Int,
    val roadName: String,
    val voicePrompt: String
)

data class TrafficIncident(
    val id: String,
    val title: String,
    val description: String,
    val distanceAheadMeters: Int,
    val type: IncidentType,
    val severity: String = "MODERATE"
)

data class CachedRegion(
    val id: String,
    val title: String,
    val sizeMb: Int,
    val isDownloaded: Boolean,
    val downloadProgress: Float = 1.0f,
    val roadsCount: String = "45,000 km",
    val description: String = "Complete offline vectors & POIs"
)

data class DestinationPreset(
    val id: String,
    val name: String,
    val address: String,
    val distanceKm: Float,
    val etaMinutes: Int,
    val speedLimitKmh: Int,
    val category: String, // "Home", "Work", "Highway", "Fuel", "Service"
    val coordinates: GeoPoint
)

data class NavigationState(
    val isNavigating: Boolean = false,
    val activeDestination: DestinationPreset? = null,
    val currentSpeedKmh: Int = 54,
    val speedLimitKmh: Int = 60,
    val isOverSpeed: Boolean = false,
    val speedLimitOvershoot: Int = 0,
    val isHudMode: Boolean = false,
    val currentManeuver: NavManeuver? = null,
    val distanceRemainingKm: Float = 0f,
    val etaMinutes: Int = 0,
    val currentRoad: String = "Mahatma Gandhi Ring Road",
    val incidentsAhead: List<TrafficIncident> = emptyList(),
    val offlineCacheActive: Boolean = true,
    val gpsSatellitesLocked: Int = 11
)
