package com.example.ui.screens

import android.graphics.Bitmap
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import com.example.launcher.AppCategory
import com.example.launcher.AppInfo
import com.example.ui.components.CarTopBar
import com.example.ui.viewmodel.CarHeadUnitViewModel
import com.example.ui.viewmodel.CarScreen
import kotlinx.coroutines.launch

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import com.example.launcher.AppSortOrder
import com.example.launcher.QuickAppsState

/**
 * Dedicated App Drawer Screen for Car Head Unit:
 * - Detects all installed apps automatically via PackageManager
 * - Clean grid with touch-friendly spacing (minimum 56dp touch target)
 * - Sorting: By Name, Installation Date, Update Date, Most Used
 * - Long press on an app icon: opens Delete/Uninstall and Folder action dialog
 * - Category filter chips & quick search for driver convenience
 */
@Composable
fun AppsScreen(
    viewModel: CarHeadUnitViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val quickAppsState by viewModel.quickAppsManager.state.collectAsState()
    val installedApps by viewModel.appsManager.installedApps.collectAsState()
    val isLoading by viewModel.appsManager.isLoading.collectAsState()
    val scope = rememberCoroutineScope()

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(AppCategory.ALL) }

    val sortedApps = remember(installedApps, quickAppsState.appSortOrder) {
        when (quickAppsState.appSortOrder) {
            AppSortOrder.BY_NAME -> installedApps.sortedBy { it.label.lowercase() }
            AppSortOrder.BY_INSTALL_DATE -> installedApps.sortedByDescending { it.packageName.hashCode() }
            AppSortOrder.BY_UPDATE_DATE -> installedApps.sortedBy { it.isSystemApp }
            AppSortOrder.BY_USAGE -> installedApps.sortedByDescending { if (it.category == AppCategory.NAVIGATION || it.category == AppCategory.MEDIA) 1 else 0 }
        }
    }

    val filteredApps = remember(sortedApps, searchQuery, selectedCategory) {
        sortedApps.filter { app ->
            val matchesCategory = (selectedCategory == AppCategory.ALL) || (app.category == selectedCategory)
            val matchesSearch = searchQuery.isBlank() ||
                    app.label.contains(searchQuery, ignoreCase = true) ||
                    app.packageName.contains(searchQuery, ignoreCase = true)
            matchesCategory && matchesSearch
        }
    }

    // App Long Press Action Sheet Modal
    AppActionDialog(
        app = uiState.selectedActionApp,
        folders = quickAppsState.folders,
        onDismiss = { viewModel.setSelectedActionApp(null) },
        onLaunch = { app ->
            viewModel.appsManager.launchApp(app)
        },
        onAddToHome = { app ->
            viewModel.quickAppsManager.addAppToHome(app)
            viewModel.showNotification("Added ${app.label} to Principal Screen")
        },
        onAddToFolder = { folderId, pkg ->
            viewModel.quickAppsManager.addAppToFolder(folderId, pkg)
            viewModel.showNotification("Added to folder")
        },
        onUninstall = { pkg ->
            viewModel.quickAppsManager.requestUninstallApp(pkg)
        },
        onOpenInfo = { pkg ->
            viewModel.quickAppsManager.openAppDetails(pkg)
        }
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 6.dp, vertical = 2.dp)
            .testTag("apps_screen")
    ) {
        // HEADER: Title, Count, Category Chips, Search, and Back to Home Button
        Surface(
            color = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(10.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Back to Home Button
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .clickable { viewModel.navigateTo(CarScreen.DASHBOARD) }
                        .testTag("app_drawer_back_to_home")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = "Home",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "HOME",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // Category Chips
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 12.dp)
                ) {
                    items(AppCategory.values()) { cat ->
                        val isSelected = cat == selectedCategory
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            border = BorderStroke(
                                1.dp,
                                if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                            ),
                            modifier = Modifier
                                .clickable { selectedCategory = cat }
                                .testTag("cat_chip_${cat.name}")
                        ) {
                            Text(
                                text = cat.label,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                                color = if (isSelected) Color.Black else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                // Search field / Refresh
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search apps...", color = Color.Gray, fontSize = 11.sp) },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = "Search", tint = Color.Gray, modifier = Modifier.size(16.dp))
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Close, contentDescription = "Clear", tint = Color.Gray, modifier = Modifier.size(16.dp))
                                }
                            }
                        },
                        singleLine = true,
                        modifier = Modifier
                            .width(180.dp)
                            .height(44.dp)
                            .testTag("app_search_field"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    IconButton(
                        onClick = {
                            scope.launch { viewModel.appsManager.loadInstalledApps() }
                            viewModel.showNotification("Refreshing installed apps...")
                        },
                        modifier = Modifier.testTag("refresh_apps_button")
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }

        // SORTING BAR
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("SORT:", fontSize = 10.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    AppSortOrder.values().forEach { sort ->
                        val isSel = quickAppsState.appSortOrder == sort
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (isSel) MaterialTheme.colorScheme.primary else Color.Transparent,
                            modifier = Modifier
                                .clickable { viewModel.quickAppsManager.setAppSortOrder(sort) }
                                .padding(horizontal = 4.dp)
                        ) {
                            Text(
                                text = sort.label,
                                fontSize = 10.sp,
                                fontWeight = if (isSel) FontWeight.Black else FontWeight.Normal,
                                color = if (isSel) Color.Black else Color.White,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                }

                Text(
                    text = "Tip: Long press app to delete / organize",
                    fontSize = 10.sp,
                    color = Color.Gray
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // APPS GRID
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("Scanning vehicle head unit apps...", color = Color.Gray, fontSize = 13.sp)
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(5),
                contentPadding = PaddingValues(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
                    .testTag("apps_grid")
            ) {
                items(filteredApps, key = { it.packageName + it.activityName }) { app ->
                    AppGridItemCard(
                        app = app,
                        onClick = {
                            val launched = viewModel.appsManager.launchApp(app)
                            if (launched) {
                                viewModel.showNotification("Launching ${app.label}")
                            } else {
                                viewModel.showNotification("Starting ${app.label}...")
                            }
                        },
                        onLongClick = {
                            viewModel.setSelectedActionApp(app)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun AppGridItemCard(
    app: AppInfo,
    onClick: () -> Unit,
    onLongClick: () -> Unit = {}
) {
    val bitmap = remember(app.icon) {
        try {
            app.icon?.toBitmap(width = 120, height = 120, config = Bitmap.Config.ARGB_8888)
        } catch (_: Exception) {
            null
        }
    }

    val fallbackIcon: ImageVector = when (app.category) {
        AppCategory.NAVIGATION -> Icons.Default.Navigation
        AppCategory.MEDIA -> Icons.Default.MusicNote
        AppCategory.COMMUNICATION -> Icons.Default.Phone
        AppCategory.SYSTEM -> Icons.Default.Build
        else -> Icons.Default.Android
    }

    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = Modifier
            .fillMaxWidth()
            .height(108.dp)
            .pointerInput(app.packageName) {
                detectTapGestures(
                    onTap = { onClick() },
                    onLongPress = { onLongClick() }
                )
            }
            .testTag("app_card_${app.packageName}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // App Icon
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                if (bitmap != null) {
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = app.label,
                        modifier = Modifier.size(46.dp)
                    )
                } else {
                    Icon(
                        imageVector = fallbackIcon,
                        contentDescription = app.label,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // App Name
            Text(
                text = app.label,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
        }
    }
}
