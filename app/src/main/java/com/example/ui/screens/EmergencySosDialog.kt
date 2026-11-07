package com.example.ui.screens

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
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PhoneInTalk
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.SpeedAlertRed
import com.example.ui.viewmodel.CarHeadUnitViewModel

@Composable
fun EmergencySosDialog(
    viewModel: CarHeadUnitViewModel,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.95f))
                .padding(20.dp)
                .testTag("emergency_sos_dialog"),
            color = Color(0xFF0F0407),
            shape = RoundedCornerShape(20.dp),
            border = androidx.compose.foundation.BorderStroke(2.dp, SpeedAlertRed)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = SpeedAlertRed,
                            modifier = Modifier.size(48.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = "SOS",
                                    tint = Color.White,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(
                                text = "EMERGENCY ROADSIDE SOS",
                                color = SpeedAlertRed,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 2.sp
                            )
                            Text(
                                text = "OFFLINE ASSISTANCE (NO DATA REQUIRED)",
                                color = Color.LightGray,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Surface(
                        shape = CircleShape,
                        color = Color(0xFF261014),
                        modifier = Modifier
                            .size(48.dp)
                            .clickable { onDismiss() }
                            .testTag("close_sos_dialog")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                        }
                    }
                }

                // GPS Coordinates Readout for Emergency Radio / Verbal Dispatch
                Surface(
                    color = Color(0xFF1E0A0F),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, SpeedAlertRed.copy(alpha = 0.6f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "OFFLINE GPS COORDINATES (READ TO OPERATOR)",
                            color = Color.LightGray,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "28° 25' 23.4\" N\n77° 05' 52.8\" E",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            color = Color.White,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            lineHeight = 38.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Elevation: 228m • NH-48 Km Stone 42.6 Landmark",
                            fontSize = 12.sp,
                            color = SpeedAlertRed,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Speak Coordinates aloud via TTS button
                        Button(
                            onClick = {
                                viewModel.ttsManager.speak(
                                    "Emergency coordinates are: 28 degrees 25 minutes North, 77 degrees 5 minutes East. Near National Highway 48 kilometer milestone 42.",
                                    isPriority = true
                                )
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B151E)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = "Speak", tint = Color.White)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("SPEAK COORDINATES OVER VEHICLE SPEAKER", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }
                }

                // DIRECT EMERGENCY HOTLINE BUTTONS (64dp Touch Targets)
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Call 112 National Emergency
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = SpeedAlertRed,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp)
                            .clickable { viewModel.dialEmergencyCall("112") }
                            .testTag("dial_112_button")
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.PhoneInTalk, contentDescription = "Call", tint = Color.White, modifier = Modifier.size(32.dp))
                                Spacer(modifier = Modifier.width(14.dp))
                                Column {
                                    Text("CALL 112 NATIONAL EMERGENCY", color = Color.White, fontWeight = FontWeight.Black, fontSize = 15.sp)
                                    Text("Police • Ambulance • Highway Patrol", color = Color.White.copy(alpha = 0.85f), fontSize = 11.sp)
                                }
                            }
                            Text("DIAL NOW", color = Color.White, fontWeight = FontWeight.Black, fontSize = 13.sp)
                        }
                    }

                    // Call Maruti Suzuki 24x7 Roadside Assistance
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFF261014),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, SpeedAlertRed.copy(alpha = 0.5f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp)
                            .clickable { viewModel.dialEmergencyCall("18001021800") }
                            .testTag("dial_maruti_sos_button")
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Call, contentDescription = "Maruti", tint = SpeedAlertRed, modifier = Modifier.size(30.dp))
                                Spacer(modifier = Modifier.width(14.dp))
                                Column {
                                    Text("MARUTI ON-ROAD ROADSIDE ASSISTANCE", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text("Towing • Flat Tire • Mechanical Recovery (1800 102 1800)", color = Color.LightGray, fontSize = 11.sp)
                                }
                            }
                            Text("CALL", color = SpeedAlertRed, fontWeight = FontWeight.Black, fontSize = 13.sp)
                        }
                    }
                }
            }
        }
    }
}
