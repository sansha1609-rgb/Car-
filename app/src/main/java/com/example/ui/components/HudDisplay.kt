package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Flip
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.North
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.navigation.ManeuverType
import com.example.navigation.NavigationState
import com.example.ui.theme.SpeedAlertRed

@Composable
fun HudDisplay(
    navState: NavigationState,
    onExitHud: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isMirrored by remember { mutableStateOf(true) }

    val hudNeonColor = if (navState.isOverSpeed) SpeedAlertRed else Color(0xFF00E5FF)

    // Pitch black background for zero glare on car windshield glass
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .testTag("hud_display_container")
    ) {
        // Content container with optional mirror inversion for glass reflection
        Column(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = if (isMirrored) -1f else 1f
                }
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top HUD row: Route Maneuver Arrow & Road
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (navState.currentManeuver != null) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val maneuverIcon = when (navState.currentManeuver.type) {
                            ManeuverType.TURN_LEFT, ManeuverType.SLIGHT_LEFT -> Icons.AutoMirrored.Filled.ArrowBack
                            ManeuverType.TURN_RIGHT, ManeuverType.SLIGHT_RIGHT -> Icons.AutoMirrored.Filled.ArrowForward
                            else -> Icons.Default.North
                        }

                        Icon(
                            imageVector = maneuverIcon,
                            contentDescription = "Turn direction",
                            tint = hudNeonColor,
                            modifier = Modifier.size(54.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "IN ${navState.currentManeuver.distanceRemainingMeters}M",
                                color = Color.White,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = navState.currentRoad.uppercase(),
                                color = Color.LightGray,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                } else {
                    Text(
                        text = "MARUTI SUZUKI CELERIO • HUD ACTIVE",
                        color = hudNeonColor,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    )
                }

                // Speed Limit International Sign (Circular White & Red)
                Surface(
                    shape = CircleShape,
                    color = Color.White,
                    border = androidx.compose.foundation.BorderStroke(4.dp, SpeedAlertRed),
                    modifier = Modifier.size(62.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "${navState.speedLimitKmh}",
                            color = Color.Black,
                            fontWeight = FontWeight.Black,
                            fontSize = 26.sp
                        )
                    }
                }
            }

            // Center: Giant HUD Speedometer
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "${navState.currentSpeedKmh}",
                    fontSize = 120.sp,
                    lineHeight = 120.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    color = hudNeonColor,
                    modifier = Modifier.testTag("hud_speed_text")
                )
                Text(
                    text = "KM/H",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 6.sp,
                    color = hudNeonColor.copy(alpha = 0.85f)
                )

                // High-visibility Speed Alert warning on windshield
                if (navState.isOverSpeed) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        color = SpeedAlertRed,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("hud_overspeed_warning")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Over speed",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "SLOW DOWN! LIMIT ${navState.speedLimitKmh} KM/H",
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            }

            // Bottom row: Distance Remaining, ETA & Highway status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (navState.isNavigating) "${navState.distanceRemainingKm} KM REMAINING" else "CRUISING",
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )

                Text(
                    text = if (navState.isNavigating) "ETA: ${navState.etaMinutes} MIN" else "OFFLINE GPS",
                    color = hudNeonColor,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        // Overlay Action Buttons (Un-mirrored for driver touch accessibility)
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Mirror Toggle Button
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFF1E293B).copy(alpha = 0.9f),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.Gray),
                modifier = Modifier
                    .size(width = 160.dp, height = 56.dp)
                    .clickable { isMirrored = !isMirrored }
                    .testTag("hud_mirror_toggle")
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Flip,
                        contentDescription = "Flip reflection",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isMirrored) "MIRROR (ON)" else "DIRECT VIEW",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }

            // Exit HUD Button
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = SpeedAlertRed.copy(alpha = 0.9f),
                modifier = Modifier
                    .size(width = 140.dp, height = 56.dp)
                    .clickable { onExitHud() }
                    .testTag("exit_hud_button")
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Exit HUD",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "EXIT HUD",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}
