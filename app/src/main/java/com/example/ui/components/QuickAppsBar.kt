package com.example.ui.components

import android.graphics.Bitmap
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import com.example.launcher.AppFolder
import com.example.launcher.AppInfo
import com.example.launcher.QuickAppSlot
import com.example.launcher.QuickAppsState
import com.example.ui.theme.SpeedAlertRed

/**
 * Customizable Quick Apps Bar on the Principal Screen:
 * - App slots for quick launching
 * - Folders for organizing apps
 * - Edit mode toggle (reorder, remove, add)
 * - Infinite scrolling and touch-friendly icon targets
 */
@Composable
fun QuickAppsBar(
    state: QuickAppsState,
    allApps: List<AppInfo>,
    onLaunchApp: (String) -> Unit,
    onAppLongClick: (AppInfo) -> Unit,
    onOpenFolder: (AppFolder) -> Unit,
    onToggleEditMode: () -> Unit,
    onRemoveSlot: (String) -> Unit,
    onAddNewAppClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 6.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: Folder Pills & Edit Toggle
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Edit button
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (state.isEditMode) SpeedAlertRed.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
                    border = BorderStroke(1.dp, if (state.isEditMode) SpeedAlertRed else MaterialTheme.colorScheme.outline),
                    modifier = Modifier
                        .height(38.dp)
                        .clickable { onToggleEditMode() }
                        .testTag("toggle_edit_quick_apps")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = null,
                            tint = if (state.isEditMode) SpeedAlertRed else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = if (state.isEditMode) "DONE" else "EDIT",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Black,
                            color = if (state.isEditMode) SpeedAlertRed else MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // Folders
                state.folders.take(3).forEach { folder ->
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = folder.color.copy(alpha = 0.16f),
                        border = BorderStroke(1.dp, folder.color.copy(alpha = 0.4f)),
                        modifier = Modifier
                            .height(38.dp)
                            .clickable { onOpenFolder(folder) }
                            .testTag("quick_folder_${folder.id}")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Folder, contentDescription = null, tint = folder.color, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(folder.name, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Vertical subtle separator
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(28.dp)
                    .background(MaterialTheme.colorScheme.outline)
            )

            Spacer(modifier = Modifier.width(6.dp))

            // Quick App Icons Row
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                items(state.selectedApps, key = { it.id }) { slot ->
                    val matchingApp = remember(slot.appPackage, allApps) {
                        allApps.find { it.packageName == slot.appPackage }
                    }

                    Box(modifier = Modifier.size(42.dp, 40.dp)) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                            modifier = Modifier
                                .fillMaxSize()
                                .pointerInput(slot.id) {
                                    detectTapGestures(
                                        onTap = {
                                            slot.appPackage?.let { onLaunchApp(it) }
                                        },
                                        onLongPress = {
                                            matchingApp?.let { onAppLongClick(it) }
                                        }
                                    )
                                }
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(2.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                val bitmap: Bitmap? = remember(matchingApp?.icon) {
                                    try { matchingApp?.icon?.toBitmap(28, 28) } catch (_: Exception) { null }
                                }
                                if (bitmap != null) {
                                    Image(bitmap = bitmap.asImageBitmap(), contentDescription = slot.label, modifier = Modifier.size(20.dp))
                                } else {
                                    Icon(Icons.Default.Android, contentDescription = slot.label, tint = state.textColorGamma.color, modifier = Modifier.size(20.dp))
                                }
                                Text(
                                    text = slot.label,
                                    fontSize = 7.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }

                        // Remove button in Edit Mode
                        if (state.isEditMode) {
                            Surface(
                                shape = CircleShape,
                                color = SpeedAlertRed,
                                modifier = Modifier
                                    .size(14.dp)
                                    .align(Alignment.TopEnd)
                                    .clickable { onRemoveSlot(slot.id) }
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.Close, contentDescription = "Remove", tint = Color.White, modifier = Modifier.size(10.dp))
                                }
                            }
                        }
                    }
                }

                // Add new app slot button in Edit Mode
                if (state.isEditMode) {
                    item {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                            modifier = Modifier
                                .size(42.dp, 40.dp)
                                .clickable { onAddNewAppClick() }
                        ) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Add, contentDescription = "Add", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}
