package com.example.ui.screens

import android.graphics.Bitmap
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.graphics.drawable.toBitmap
import com.example.launcher.AppFolder
import com.example.launcher.AppInfo
import com.example.ui.theme.SpeedAlertRed

/**
 * Long-Press App Action Sheet:
 * Allows uninstalling/deleting an app, adding it to the principal screen,
 * adding it to a folder, or opening system info.
 */
@Composable
fun AppActionDialog(
    app: AppInfo?,
    folders: List<AppFolder>,
    onDismiss: () -> Unit,
    onLaunch: (AppInfo) -> Unit,
    onAddToHome: (AppInfo) -> Unit,
    onAddToFolder: (String, String) -> Unit,
    onUninstall: (String) -> Unit,
    onOpenInfo: (String) -> Unit
) {
    if (app == null) return

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = Color(0xFF101726),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)),
            modifier = Modifier
                .width(380.dp)
                .testTag("app_action_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // App Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val iconBitmap: Bitmap? = remember(app.icon) {
                            try { app.icon?.toBitmap(56, 56) } catch (_: Exception) { null }
                        }
                        if (iconBitmap != null) {
                            Image(bitmap = iconBitmap.asImageBitmap(), contentDescription = app.label, modifier = Modifier.size(46.dp))
                        } else {
                            Icon(Icons.Default.Android, contentDescription = app.label, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(46.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(app.label, fontSize = 15.sp, fontWeight = FontWeight.Black, color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text(app.packageName, fontSize = 10.sp, color = Color.Gray, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Action 1: Launch App
                ActionButton(
                    icon = Icons.Default.PlayArrow,
                    title = "Launch Application",
                    tint = MaterialTheme.colorScheme.primary,
                    onClick = {
                        onLaunch(app)
                        onDismiss()
                    }
                )

                // Action 2: Add to Principal Screen Quick Apps
                ActionButton(
                    icon = Icons.Default.Add,
                    title = "Add to Principal Screen",
                    tint = Color(0xFF00E676),
                    onClick = {
                        onAddToHome(app)
                        onDismiss()
                    }
                )

                // Action 3: Add to Folders
                if (folders.isNotEmpty()) {
                    Text("Add to Folder:", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        folders.forEach { f ->
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = f.color.copy(alpha = 0.15f),
                                border = BorderStroke(1.dp, f.color.copy(alpha = 0.4f)),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        onAddToFolder(f.id, app.packageName)
                                        onDismiss()
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(Icons.Default.Folder, contentDescription = null, tint = f.color, modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(f.name.take(10), fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                // Action 4: System App Info
                ActionButton(
                    icon = Icons.Default.Info,
                    title = "Application Details & Permissions",
                    tint = Color.White,
                    onClick = {
                        onOpenInfo(app.packageName)
                        onDismiss()
                    }
                )

                // Action 5: Uninstall / Delete Application (Red warning button)
                Button(
                    onClick = {
                        onUninstall(app.packageName)
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SpeedAlertRed.copy(alpha = 0.2f)),
                    border = BorderStroke(1.dp, SpeedAlertRed.copy(alpha = 0.6f)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, tint = SpeedAlertRed, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("UNINSTALL / DELETE APPLICATION", color = SpeedAlertRed, fontSize = 11.sp, fontWeight = FontWeight.Black)
                }
            }
        }
    }
}

@Composable
private fun ActionButton(
    icon: ImageVector,
    title: String,
    tint: Color,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = Color.White.copy(alpha = 0.05f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(10.dp))
            Text(title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
    }
}
