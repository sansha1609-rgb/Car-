package com.example.ui.screens

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.CarHeadUnitViewModel

data class FmStationPreset(
    val frequencyMhz: Float,
    val name: String,
    val tagline: String
)

/**
 * Dedicated In-Car FM Radio Screen:
 * - Direct replacement for Music tab as requested by user
 * - Uses device's built-in FM Radio hardware / apps (com.android.fmradio)
 * - Pure Black and White OLED dark cockpit theme with Aptos typography
 * - Full immersion (status bar hidden)
 * - Large high-contrast digital frequency readout
 * - Interactive FM tuner dial (87.5 MHz - 108.0 MHz)
 * - Fast seek / step buttons (-0.1, +0.1, Scan Down, Scan Up)
 * - Station presets for popular automotive radio stations
 * - Direct launcher button to open system built-in FM Radio app
 */
@Composable
fun FmRadioScreen(
    viewModel: CarHeadUnitViewModel,
    modifier: Modifier = Modifier
) {
    val fmState by viewModel.audioPlayer.fmState.collectAsState()
    var currentFrequency by remember { mutableFloatStateOf(98.3f) }
    var isMuted by remember { mutableStateOf(false) }

    val stationPresets = remember {
        listOf(
            FmStationPreset(91.1f, "Radio City", "Rag Rag Mein Daude City"),
            FmStationPreset(92.7f, "Big FM", "Suno Sunao, Life Banao"),
            FmStationPreset(93.5f, "Red FM", "Bajaate Raho! Superhits"),
            FmStationPreset(98.3f, "Radio Mirchi", "Mirchi Sunne Wale Always Khush"),
            FmStationPreset(104.0f, "Fever FM", "Baap of Bollywood"),
            FmStationPreset(106.4f, "AIR FM Rainbow", "National Public Broadcast"),
            FmStationPreset(107.2f, "Car Link FM", "Automotive Head Unit Stereo")
        )
    }

    val currentStationName = stationPresets.firstOrNull {
        kotlin.math.abs(it.frequencyMhz - currentFrequency) < 0.05f
    }?.name ?: "FM Stereo Tuner"

    val currentTagline = stationPresets.firstOrNull {
        kotlin.math.abs(it.frequencyMhz - currentFrequency) < 0.05f
    }?.tagline ?: "Direct Analog In-Car Reception"

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF000000))
            .padding(horizontal = 10.dp, vertical = 6.dp)
            .testTag("fm_radio_screen"),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // TOP HERO FM TUNER CARD
        Surface(
            color = Color(0xFF0D0D0D),
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, Color(0xFF262626)),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1.3f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Radio, contentDescription = "FM", tint = Color.White, modifier = Modifier.size(18.dp))
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("FM RADIO TUNER", fontSize = 12.sp, fontWeight = FontWeight.Black, color = Color.White, letterSpacing = 0.5.sp)
                            Text("Built-in Vehicle Receiver", fontSize = 9.sp, color = Color.Gray)
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        // Stereo Badge
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color.White.copy(alpha = 0.12f),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.3f))
                        ) {
                            Text("STEREO • RDS", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp))
                        }

                        // Mute Button
                        IconButton(
                            onClick = {
                                isMuted = !isMuted
                                if (isMuted) viewModel.audioPlayer.pause() else viewModel.audioPlayer.play()
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = if (isMuted) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                                contentDescription = "Mute",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        // Primary Action: Launch device's built-in FM Radio App
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFF1F1F1F),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.5f)),
                            modifier = Modifier
                                .clickable { viewModel.appsManager.launchFmRadio() }
                                .testTag("launch_system_fm_radio_btn")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.OpenInNew, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("OPEN SYSTEM FM APP", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                }

                // Center Digital Frequency & Station Info
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = String.format("%.1f", currentFrequency),
                            fontSize = 54.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            color = Color.White,
                            letterSpacing = (-1).sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "MHz",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.LightGray,
                            modifier = Modifier.padding(bottom = 10.dp)
                        )
                    }

                    Text(
                        text = currentStationName,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = currentTagline,
                        fontSize = 10.sp,
                        color = Color.Gray
                    )
                }

                // Interactive FM Tuner Scale Slider (87.5 - 108.0 MHz)
                Column(modifier = Modifier.fillMaxWidth()) {
                    Slider(
                        value = currentFrequency,
                        onValueChange = {
                            currentFrequency = (kotlin.math.round(it * 10f) / 10f)
                            viewModel.audioPlayer.setFmFrequency(currentFrequency)
                        },
                        valueRange = 87.5f..108.0f,
                        colors = SliderDefaults.colors(
                            thumbColor = Color.White,
                            activeTrackColor = Color.White,
                            inactiveTrackColor = Color(0xFF333333)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("fm_frequency_slider")
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("87.5", fontSize = 9.sp, color = Color.Gray, fontFamily = FontFamily.Monospace)
                        Text("92.0", fontSize = 9.sp, color = Color.Gray, fontFamily = FontFamily.Monospace)
                        Text("98.0", fontSize = 9.sp, color = Color.Gray, fontFamily = FontFamily.Monospace)
                        Text("104.0", fontSize = 9.sp, color = Color.Gray, fontFamily = FontFamily.Monospace)
                        Text("108.0 MHz", fontSize = 9.sp, color = Color.Gray, fontFamily = FontFamily.Monospace)
                    }
                }

                // Fine Tuning & Seek Controls Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Seek Down
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF1A1A1A),
                        border = BorderStroke(1.dp, Color(0xFF333333)),
                        modifier = Modifier
                            .clickable {
                                val prevStation = stationPresets.lastOrNull { it.frequencyMhz < currentFrequency - 0.05f }
                                currentFrequency = prevStation?.frequencyMhz ?: (currentFrequency - 0.5f).coerceAtLeast(87.5f)
                                viewModel.audioPlayer.setFmFrequency(currentFrequency)
                            }
                            .testTag("fm_seek_prev")
                    ) {
                        Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.FastRewind, contentDescription = "Seek Down", tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("SEEK -", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }

                    // -0.1 Step
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF1A1A1A),
                        border = BorderStroke(1.dp, Color(0xFF333333)),
                        modifier = Modifier
                            .clickable {
                                currentFrequency = ((currentFrequency - 0.1f) * 10f).let { kotlin.math.round(it) / 10f }.coerceAtLeast(87.5f)
                                viewModel.audioPlayer.setFmFrequency(currentFrequency)
                            }
                            .testTag("fm_step_down")
                    ) {
                        Text("-0.1 MHz", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp))
                    }

                    // +0.1 Step
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF1A1A1A),
                        border = BorderStroke(1.dp, Color(0xFF333333)),
                        modifier = Modifier
                            .clickable {
                                currentFrequency = ((currentFrequency + 0.1f) * 10f).let { kotlin.math.round(it) / 10f }.coerceAtMost(108.0f)
                                viewModel.audioPlayer.setFmFrequency(currentFrequency)
                            }
                            .testTag("fm_step_up")
                    ) {
                        Text("+0.1 MHz", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp))
                    }

                    // Seek Up
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF1A1A1A),
                        border = BorderStroke(1.dp, Color(0xFF333333)),
                        modifier = Modifier
                            .clickable {
                                val nextStation = stationPresets.firstOrNull { it.frequencyMhz > currentFrequency + 0.05f }
                                currentFrequency = nextStation?.frequencyMhz ?: (currentFrequency + 0.5f).coerceAtMost(108.0f)
                                viewModel.audioPlayer.setFmFrequency(currentFrequency)
                            }
                            .testTag("fm_seek_next")
                    ) {
                        Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text("SEEK +", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(Icons.Default.FastForward, contentDescription = "Seek Up", tint = Color.White, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }

        // POPULAR STATION PRESETS ROW
        Surface(
            color = Color(0xFF0D0D0D),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, Color(0xFF262626)),
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.7f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("STATION PRESETS", fontSize = 10.sp, fontWeight = FontWeight.Black, color = Color.White, letterSpacing = 0.5.sp)
                    Text("Tap to Tune", fontSize = 9.sp, color = Color.Gray)
                }

                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(stationPresets) { preset ->
                        val isSelected = kotlin.math.abs(preset.frequencyMhz - currentFrequency) < 0.05f
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) Color.White else Color(0xFF1A1A1A),
                            border = BorderStroke(1.dp, if (isSelected) Color.White else Color(0xFF333333)),
                            modifier = Modifier
                                .width(135.dp)
                                .fillMaxHeight()
                                .clickable {
                                    currentFrequency = preset.frequencyMhz
                                    viewModel.audioPlayer.setFmFrequency(preset.frequencyMhz)
                                }
                                .testTag("fm_preset_${preset.frequencyMhz}")
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(6.dp),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${preset.frequencyMhz}",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Black,
                                        fontFamily = FontFamily.Monospace,
                                        color = if (isSelected) Color.Black else Color.White
                                    )
                                    Text(
                                        text = "FM",
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color.Black else Color.Gray
                                    )
                                }

                                Column {
                                    Text(
                                        text = preset.name,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color.Black else Color.White,
                                        maxLines = 1
                                    )
                                    Text(
                                        text = preset.tagline,
                                        fontSize = 8.sp,
                                        color = if (isSelected) Color.DarkGray else Color.Gray,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
