package com.example.ui.screens

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import com.example.ui.theme.AptosFontFamily

enum class ScreenSaverPrototype(val label: String) {
    DIGITAL_OLED("Digital OLED Minimal"),
    ANALOG_CHRONO("Classic Chronometer Dials"),
    RETRO_FLIP("Retro Flip Clock"),
    NIGHT_HUD("Ambient Night HUD")
}

enum class ClockFontChoice(val label: String, val fontFamily: FontFamily) {
    APTOS("Aptos Normal", AptosFontFamily),
    MONOSPACE("Digital Tech Mono", FontFamily.Monospace),
    SANS_SERIF("Modern Automotive Sans", FontFamily.SansSerif),
    SERIF("Luxury Chrono Serif", FontFamily.Serif),
    CURSIVE("Dynamic Sport", FontFamily.Cursive)
}

data class ScreenSaverSettings(
    val prototype: ScreenSaverPrototype = ScreenSaverPrototype.DIGITAL_OLED,
    val fontChoice: ClockFontChoice = ClockFontChoice.APTOS,
    val dateFormatPattern: String = "EEEE, d MMMM",
    val clockColor: Color = Color(0xFFFFFFFF),
    val clockSizeSp: Int = 84,
    val showSeconds: Boolean = true,
    val showDate: Boolean = true,
    val showBatteryVoltage: Boolean = true,
    val showSpeedMini: Boolean = true,
    val burnInDriftEnabled: Boolean = true,
    val dimBrightness: Boolean = true
)
