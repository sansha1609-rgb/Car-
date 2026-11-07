package com.example.ui.screens

import android.graphics.Bitmap
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.graphics.drawable.toBitmap
import com.example.launcher.AppFolder
import com.example.launcher.AppInfo

/**
 * App Folder Dialog for Principal Screen:
 * Displays apps inside the selected folder. Allows launching apps,
 * adding new apps into this folder, and quick navigation.
 */
@Composable
fun AppFolderDialog(
    folder: AppFolder?,
    allInstalledApps: List<AppInfo>,
    onDismiss: () -> Unit,
    onLaunchApp: (AppInfo) -> Unit,
    onAddAppToFolder: (String, String) -> Unit
) {
    if (folder == null) return

    var isAddingApp by remember { mutableStateOf(false) }

    val folderApps = remember(folder, allInstalledApps) {
        allInstalledApps.filter { folder.appPackages.contains(it.packageName) }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color(0xFF0F172A).copy(alpha = 0.98f),
            border = BorderStroke(1.5.dp, folder.color),
            modifier = Modifier
                .width(480.dp)
                .testTag("app_folder_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
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
                            color = folder.color.copy(alpha = 0.2f),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Folder, contentDescription = null, tint = folder.color, modifier = Modifier.size(20.dp))
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(folder.name, fontSize = 16.sp, fontWeight = FontWeight.Black, color = Color.White)
                            Text("${folderApps.size} applications", fontSize = 11.sp, color = Color.Gray)
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = folder.color.copy(alpha = 0.15f),
                            border = BorderStroke(1.dp, folder.color.copy(alpha = 0.4f)),
                            modifier = Modifier.clickable { isAddingApp = !isAddingApp }
                        ) {
                            Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Add, contentDescription = null, tint = folder.color, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(if (isAddingApp) "DONE" else "ADD APP", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = folder.color)
                            }
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (isAddingApp) {
                    Text("Select an app to add to ${folder.name}:", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(4),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(240.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        val availableToAdd = allInstalledApps.filter { !folder.appPackages.contains(it.packageName) }
                        items(availableToAdd) { app ->
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color.White.copy(alpha = 0.05f),
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
                                modifier = Modifier
                                    .clickable {
                                        onAddAppToFolder(folder.id, app.packageName)
                                    }
                                    .padding(4.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    val iconBitmap: Bitmap? = remember(app.icon) {
                                        try { app.icon?.toBitmap(48, 48) } catch (_: Exception) { null }
                                    }
                                    if (iconBitmap != null) {
                                        Image(bitmap = iconBitmap.asImageBitmap(), contentDescription = app.label, modifier = Modifier.size(36.dp))
                                    } else {
                                        Icon(Icons.Default.Android, contentDescription = app.label, tint = folder.color, modifier = Modifier.size(36.dp))
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(app.label, fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, color = Color.White)
                                }
                            }
                        }
                    }
                } else {
                    if (folderApps.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No apps in this folder yet.\nTap 'ADD APP' to assign apps.", color = Color.Gray, textAlign = TextAlign.Center, fontSize = 12.sp)
                        }
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(4),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(folderApps) { app ->
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color.White.copy(alpha = 0.05f),
                                    border = BorderStroke(1.dp, folder.color.copy(alpha = 0.3f)),
                                    modifier = Modifier
                                        .clickable {
                                            onLaunchApp(app)
                                            onDismiss()
                                        }
                                        .padding(4.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(8.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        val iconBitmap: Bitmap? = remember(app.icon) {
                                            try { app.icon?.toBitmap(56, 56) } catch (_: Exception) { null }
                                        }
                                        if (iconBitmap != null) {
                                            Image(bitmap = iconBitmap.asImageBitmap(), contentDescription = app.label, modifier = Modifier.size(44.dp))
                                        } else {
                                            Icon(Icons.Default.Android, contentDescription = app.label, tint = folder.color, modifier = Modifier.size(44.dp))
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = app.label,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            textAlign = TextAlign.Center
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
}
