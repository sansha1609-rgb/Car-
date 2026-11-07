package com.example.launcher

import androidx.compose.ui.graphics.Color

enum class LauncherScreenTheme(val displayName: String, val subtitle: String) {
    COCKPIT_MODERN("Cockpit Modern", "Large central speedometer cluster, navigation & media cards"),
    CLASSIC_ANALOG("Classic Analog Gauges", "Dual analog speedometer & tachometer dials with chronometer"),
    AGAMA_TECH("Agama Tech Grid", "Signature automotive circular hub with surrounding multi-widgets")
}

enum class CarBrandLogo(val displayName: String, val shortTag: String) {
    MARUTI_SUZUKI("Maruti Suzuki", "MS"),
    SUZUKI("Suzuki", "S"),
    HONDA("Honda", "H"),
    TOYOTA("Toyota", "T"),
    HYUNDAI("Hyundai", "H"),
    FORD("Ford", "F"),
    VOLKSWAGEN("Volkswagen", "VW"),
    BMW("BMW", "BMW"),
    MERCEDES("Mercedes-Benz", "MB"),
    AUDI("Audi", "AUDI"),
    NISSAN("Nissan", "N"),
    KIA("Kia", "KIA"),
    TATA("Tata Motors", "TATA"),
    MAHINDRA("Mahindra", "M"),
    CAR_LAUNCHER_PRO("Car Launcher CL", "CL")
}

enum class TextColorGamma(val displayName: String, val color: Color) {
    CYAN("Celerio Cyan", Color(0xFF00E5FF)),
    AMBER("Night Cockpit Amber", Color(0xFFFF9100)),
    RACING_RED("Sport Racing Red", Color(0xFFFF1744)),
    RALLY_GREEN("Rally Emerald Green", Color(0xFF00E676)),
    ELECTRIC_BLUE("Electric Blue", Color(0xFF2979FF)),
    PURE_WHITE("Clean Pure White", Color(0xFFFFFFFF)),
    SPEED_GOLD("GT Championship Gold", Color(0xFFFFD700))
}

data class AppFolder(
    val id: String,
    val name: String,
    val color: Color = Color(0xFF00E5FF),
    val appPackages: List<String> = emptyList()
)

data class QuickAppSlot(
    val id: String,
    val appPackage: String? = null,
    val appActivity: String? = null,
    val label: String,
    val folderId: String? = null
)

data class WeatherInfo(
    val temperatureC: Int = 28,
    val condition: String = "Clear & Sunny",
    val locationName: String = "New Delhi, Express Corridor",
    val humidityPercent: Int = 54,
    val windSpeedKmh: Int = 14,
    val iconCode: String = "01d"
)

data class AddressInfo(
    val street: String = "NH-48 Express Highway (KM 42.5)",
    val city: String = "Gurugram / NCR",
    val latitude: Double = 28.4595,
    val longitude: Double = 77.0266,
    val headingDegrees: Float = 64f,
    val compassDirection: String = "ENE",
    val altitudeMeters: Int = 224
)
