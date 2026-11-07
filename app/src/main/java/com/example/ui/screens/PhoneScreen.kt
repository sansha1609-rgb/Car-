package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.BluetoothConnected
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PhoneCallback
import androidx.compose.material.icons.filled.VolumeDown
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.SpeedAlertRed
import com.example.ui.viewmodel.CarHeadUnitViewModel

data class CarContact(val name: String, val number: String, val subtitle: String, val isEmergency: Boolean = false)

/**
 * Dedicated In-Car Phone Dialer & Active Call Management Screen:
 * - Fix & enable dialer so calls can be made and received (CUJ complete)
 * - Pure Black and White dark theme with Aptos typography
 * - Full immersion (status bar hidden)
 * - Active call interface with live duration timer, mute, speakerphone, end call
 * - Incoming call notification overlay with Answer and Decline
 * - Large touch targets for safe automotive driving interaction
 * - Speed dial presets & System Phone app launcher
 */
@Composable
fun PhoneScreen(
    viewModel: CarHeadUnitViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val sysStatus by viewModel.systemStatusManager.status.collectAsState()
    val activeCall by viewModel.activeCall.collectAsState()

    val quickContacts = listOf(
        CarContact("Home", "+91 98765 43210", "Speed Dial 1"),
        CarContact("Work / Office", "+91 98111 22334", "Speed Dial 2"),
        CarContact("Emergency Services", "112", "National SOS Dispatch", isEmergency = true),
        CarContact("Roadside Assistance", "1800 102 1800", "24/7 Maruti Suzuki On-Road"),
        CarContact("Automotive Service Station", "+91 124 4567890", "Gurgaon Workshop")
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF000000))
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .testTag("phone_screen")
    ) {
        // INCOMING CALL BANNER / ALERT
        AnimatedVisibility(visible = activeCall.isIncoming) {
            Surface(
                color = Color(0xFF141414),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.5.dp, Color.White),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color.White),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.PhoneCallback, contentDescription = null, tint = Color.Black, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("INCOMING CALL...", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.LightGray)
                            Text(activeCall.callerName, fontSize = 15.sp, fontWeight = FontWeight.Black, color = Color.White)
                            Text(activeCall.callerNumber, fontSize = 11.sp, color = Color.Gray, fontFamily = FontFamily.Monospace)
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        // Decline
                        Button(
                            onClick = { viewModel.endCall() },
                            colors = ButtonDefaults.buttonColors(containerColor = SpeedAlertRed),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(38.dp)
                        ) {
                            Icon(Icons.Default.CallEnd, contentDescription = "Decline", tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("DECLINE", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }

                        // Answer
                        Button(
                            onClick = { viewModel.answerCall() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(38.dp)
                        ) {
                            Icon(Icons.Default.Call, contentDescription = "Answer", tint = Color.Black, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("ANSWER", fontSize = 11.sp, fontWeight = FontWeight.Black, color = Color.Black)
                        }
                    }
                }
            }
        }

        // ACTIVE IN-CALL BAR
        AnimatedVisibility(visible = activeCall.isInCall) {
            Surface(
                color = Color(0xFF111111),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color.White),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp)
                    .testTag("active_call_bar")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Call, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("CALL IN PROGRESS • ", fontSize = 9.sp, fontWeight = FontWeight.Normal, color = Color.LightGray)
                                val sec = activeCall.durationSeconds
                                Text(
                                    text = String.format("%02d:%02d", sec / 60, sec % 60),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    fontFamily = FontFamily.Monospace,
                                    color = Color.White
                                )
                            }
                            Text(
                                text = "${activeCall.callerName} (${activeCall.callerNumber})",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    // In-Call Controls
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Mute Mic
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (activeCall.isMuted) Color.White else Color(0xFF222222),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.5f)),
                            modifier = Modifier.clickable { viewModel.toggleCallMute() }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (activeCall.isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                                    contentDescription = "Mute",
                                    tint = if (activeCall.isMuted) Color.Black else Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (activeCall.isMuted) "MUTED" else "MUTE",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (activeCall.isMuted) Color.Black else Color.White
                                )
                            }
                        }

                        // Speakerphone
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (activeCall.isSpeakerOn) Color.White else Color(0xFF222222),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.5f)),
                            modifier = Modifier.clickable { viewModel.toggleCallSpeaker() }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (activeCall.isSpeakerOn) Icons.Default.VolumeUp else Icons.Default.VolumeDown,
                                    contentDescription = "Speaker",
                                    tint = if (activeCall.isSpeakerOn) Color.Black else Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (activeCall.isSpeakerOn) "SPEAKER" else "EARPIECE",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (activeCall.isSpeakerOn) Color.Black else Color.White
                                )
                            }
                        }

                        // Red End Call Button
                        Button(
                            onClick = { viewModel.endCall() },
                            colors = ButtonDefaults.buttonColors(containerColor = SpeedAlertRed),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .height(36.dp)
                                .testTag("btn_end_call")
                        ) {
                            Icon(Icons.Default.CallEnd, contentDescription = "End Call", tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("END CALL", fontSize = 10.sp, fontWeight = FontWeight.Black, color = Color.White)
                        }
                    }
                }
            }
        }

        // 2-PANE DIALER & CONTACTS LAYOUT
        Row(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // LEFT COLUMN: Keypad
            Surface(
                color = Color(0xFF0D0D0D),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color(0xFF262626)),
                modifier = Modifier
                    .weight(1.1f)
                    .fillMaxHeight()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Number Display Strip
                    Surface(
                        color = Color(0xFF161616),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, Color(0xFF333333)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = if (uiState.dialedNumber.isEmpty()) "Enter number..." else uiState.dialedNumber,
                                fontSize = if (uiState.dialedNumber.isEmpty()) 13.sp else 22.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = if (uiState.dialedNumber.isEmpty()) Color.Gray else Color.White,
                                modifier = Modifier.testTag("dialed_number_display")
                            )

                            if (uiState.dialedNumber.isNotEmpty()) {
                                IconButton(
                                    onClick = { viewModel.deleteDialDigit() },
                                    modifier = Modifier.testTag("dialpad_backspace")
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.Backspace,
                                        contentDescription = "Backspace",
                                        tint = Color.White
                                    )
                                }
                            }
                        }
                    }

                    // Keypad Grid: 1-9, *, 0, #
                    val keypadRows = listOf(
                        listOf("1" to "", "2" to "ABC", "3" to "DEF"),
                        listOf("4" to "GHI", "5" to "JKL", "6" to "MNO"),
                        listOf("7" to "PQRS", "8" to "TUV", "9" to "WXYZ"),
                        listOf("*" to "", "0" to "+", "#" to "")
                    )

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        keypadRows.forEach { row ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                row.forEach { (digit, sub) ->
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = Color(0xFF1A1A1A),
                                        border = BorderStroke(1.dp, Color(0xFF2A2A2A)),
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(48.dp)
                                            .clickable { viewModel.appendDialDigit(digit) }
                                            .testTag("dialpad_btn_$digit")
                                    ) {
                                        Column(
                                            modifier = Modifier.fillMaxSize(),
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center
                                        ) {
                                            Text(digit, fontSize = 18.sp, fontWeight = FontWeight.Black, color = Color.White)
                                            if (sub.isNotEmpty()) {
                                                Text(sub, fontSize = 7.sp, fontWeight = FontWeight.SemiBold, color = Color.Gray)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Call & Clear Buttons Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Clear button
                        if (uiState.dialedNumber.isNotEmpty()) {
                            Button(
                                onClick = { viewModel.clearDialDigits() },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF222222)),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .weight(0.4f)
                                    .height(46.dp)
                            ) {
                                Text("Clear", color = Color.LightGray, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                        }

                        // Large White Call Button
                        Button(
                            onClick = { viewModel.callDialedNumber() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .testTag("dialpad_call_button")
                        ) {
                            Icon(Icons.Default.Call, contentDescription = "Call", tint = Color.Black, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("CALL", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 14.sp)
                        }
                    }
                }
            }

            // RIGHT COLUMN: Connected Bluetooth Device, Speed Dial & System Shortcuts
            Surface(
                color = Color(0xFF0D0D0D),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color(0xFF262626)),
                modifier = Modifier
                    .weight(0.9f)
                    .fillMaxHeight()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(10.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Connected Phone Status Banner
                    Surface(
                        color = Color(0xFF161616),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, Color(0xFF2E2E2E)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.BluetoothConnected, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("BLUETOOTH AUDIO & CALLS", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color.LightGray)
                                Text(sysStatus.connectedDeviceName, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("SPEED DIAL PRESETS", fontSize = 10.sp, fontWeight = FontWeight.Black, color = Color.White)
                        // Trigger test incoming call
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0xFF1F1F1F),
                            border = BorderStroke(1.dp, Color(0xFF333333)),
                            modifier = Modifier.clickable { viewModel.simulateIncomingCall() }
                        ) {
                            Text("TEST INCOMING", fontSize = 7.sp, fontWeight = FontWeight.Bold, color = Color.LightGray, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                        }
                    }

                    // Quick Contacts List
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(quickContacts) { contact ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (contact.isEmergency) SpeedAlertRed.copy(alpha = 0.15f) else Color(0xFF161616),
                                border = BorderStroke(
                                    1.dp,
                                    if (contact.isEmergency) SpeedAlertRed else Color(0xFF2B2B2B)
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.updateDialedNumber(contact.number)
                                        viewModel.startCall(contact.number, contact.name)
                                    }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 8.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = if (contact.isEmergency) Icons.Default.Warning else Icons.Default.Person,
                                            contentDescription = null,
                                            tint = if (contact.isEmergency) SpeedAlertRed else Color.White,
                                            modifier = Modifier.size(15.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Column {
                                            Text(contact.name, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                            Text(contact.number, fontSize = 9.sp, color = Color.Gray, fontFamily = FontFamily.Monospace)
                                        }
                                    }
                                    Icon(Icons.Default.Call, contentDescription = "Call", tint = Color.White, modifier = Modifier.size(14.dp))
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // System App Shortcuts
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Button(
                            onClick = { viewModel.appsManager.launchContacts() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A1A1A)),
                            border = BorderStroke(1.dp, Color(0xFF333333)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                        ) {
                            Icon(Icons.Default.Contacts, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Contacts", fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Normal)
                        }

                        Button(
                            onClick = { viewModel.appsManager.launchDialer() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A1A1A)),
                            border = BorderStroke(1.dp, Color(0xFF333333)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                        ) {
                            Icon(Icons.Default.Phone, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("System Phone", fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Normal)
                        }
                    }
                }
            }
        }
    }
}
