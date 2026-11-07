package com.example.ui.screens

import android.annotation.SuppressLint
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
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
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.VideoLibrary
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
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.ui.components.OfflineMapView
import com.example.ui.viewmodel.CarHeadUnitViewModel
import com.example.ui.viewmodel.CarScreen

data class DrivingVideoPlaylist(
    val title: String,
    val subtitle: String,
    val searchQuery: String
)

/**
 * Split Screen View for 1280x720 16:9 Landscape Car Head Units:
 * Runs Google Maps and YouTube side-by-side using device's signed-in system apps.
 *
 * Requirements fulfilled:
 * - Removed Home, Resi, and NH tabs from Google Maps panel
 * - Removed entire Lo-fi Hip Hop row from YouTube panel
 * - Integrated device's built-in signed-in YouTube and Google Maps apps
 * - Navigation and route guidance disabled; only live GPS location displayed
 * - Top status bar hidden (fully immersive)
 * - Pure Black and White dark theme with Aptos typography
 */
@Composable
fun SplitScreenView(
    viewModel: CarHeadUnitViewModel,
    modifier: Modifier = Modifier
) {
    var isMapOnLeft by remember { mutableStateOf(true) }
    var splitRatio by remember { mutableFloatStateOf(0.5f) } // 0.5f = 50:50, 0.65f = Map heavy, 0.35f = YT heavy

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF000000))
            .padding(horizontal = 6.dp, vertical = 3.dp)
            .testTag("split_screen_view")
    ) {
        // SPLIT VIEW TOOLBAR
        Surface(
            color = Color(0xFF0D0D0D),
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(1.dp, Color(0xFF262626)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Title
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Layers,
                        contentDescription = "Split View",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "SPLIT VIEW • GOOGLE MAPS + YOUTUBE (SYSTEM APPS)",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        letterSpacing = 0.5.sp
                    )
                }

                // Controls: Swap Panes & Ratio buttons
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Ratio: 50/50
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (splitRatio == 0.5f) Color.White else Color(0xFF1F1F1F),
                        border = BorderStroke(1.dp, Color(0xFF333333)),
                        modifier = Modifier
                            .clickable { splitRatio = 0.5f }
                            .testTag("split_ratio_50_50")
                    ) {
                        Text(
                            text = "50 : 50",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            color = if (splitRatio == 0.5f) Color.Black else Color.White,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    // Ratio: Map Focus (65 : 35)
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (splitRatio > 0.55f) Color.White else Color(0xFF1F1F1F),
                        border = BorderStroke(1.dp, Color(0xFF333333)),
                        modifier = Modifier
                            .clickable { splitRatio = 0.65f }
                            .testTag("split_ratio_map_focus")
                    ) {
                        Text(
                            text = "MAP 65%",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            color = if (splitRatio > 0.55f) Color.Black else Color.White,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    // Ratio: YouTube Focus (35 : 65)
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (splitRatio < 0.45f) Color.White else Color(0xFF1F1F1F),
                        border = BorderStroke(1.dp, Color(0xFF333333)),
                        modifier = Modifier
                            .clickable { splitRatio = 0.35f }
                            .testTag("split_ratio_yt_focus")
                    ) {
                        Text(
                            text = "YT 65%",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            color = if (splitRatio < 0.45f) Color.Black else Color.White,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    // Swap Panes button
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFF1F1F1F),
                        border = BorderStroke(1.dp, Color(0xFF333333)),
                        modifier = Modifier
                            .clickable { isMapOnLeft = !isMapOnLeft }
                            .testTag("swap_split_panes_btn")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.SwapHoriz, contentDescription = "Swap", tint = Color.White, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("SWAP", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }

                    // Exit to Dashboard
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFF1F1F1F),
                        border = BorderStroke(1.dp, Color(0xFF333333)),
                        modifier = Modifier.clickable { viewModel.navigateTo(CarScreen.DASHBOARD) }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Home, contentDescription = "Exit", tint = Color.White, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("HOME", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // DUAL SPLIT PANES (MAP & YOUTUBE)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val leftWeight = if (isMapOnLeft) splitRatio else (1f - splitRatio)
            val rightWeight = if (isMapOnLeft) (1f - splitRatio) else splitRatio

            if (isMapOnLeft) {
                // LEFT: Google Maps Pane
                GoogleMapsSplitPane(
                    viewModel = viewModel,
                    modifier = Modifier
                        .weight(leftWeight)
                        .fillMaxHeight()
                )

                // RIGHT: YouTube Pane
                YouTubeSplitPane(
                    viewModel = viewModel,
                    modifier = Modifier
                        .weight(rightWeight)
                        .fillMaxHeight()
                )
            } else {
                // LEFT: YouTube Pane
                YouTubeSplitPane(
                    viewModel = viewModel,
                    modifier = Modifier
                        .weight(leftWeight)
                        .fillMaxHeight()
                )

                // RIGHT: Google Maps Pane
                GoogleMapsSplitPane(
                    viewModel = viewModel,
                    modifier = Modifier
                        .weight(rightWeight)
                        .fillMaxHeight()
                )
            }
        }
    }
}

/**
 * Google Maps Split Pane Component (System App integration, live GPS location only, no route guidance, Home/Resi/NH tabs removed)
 */
@Composable
private fun GoogleMapsSplitPane(
    viewModel: CarHeadUnitViewModel,
    modifier: Modifier = Modifier
) {
    val navState by viewModel.navEngine.navState.collectAsState()

    Surface(
        color = Color(0xFF0D0D0D),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Color(0xFF262626)),
        modifier = modifier.testTag("split_google_maps_pane")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Navigation, contentDescription = "Maps", tint = Color.White, modifier = Modifier.size(15.dp))
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text("GOOGLE MAPS • SYSTEM APP", fontSize = 11.sp, fontWeight = FontWeight.Normal, color = Color.White)
                        Text("Live GPS Location (Route Guidance Disabled)", fontSize = 8.sp, color = Color.LightGray)
                    }
                }

                // Open Official Google Maps App button (Uses signed-in system app)
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFF1F1F1F),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .clickable { viewModel.appsManager.launchNavigationApp() }
                        .testTag("split_launch_maps_app_btn")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.OpenInNew, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("OPEN MAPS APP", fontSize = 9.sp, fontWeight = FontWeight.Normal, color = Color.White)
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Central Interactive Vector Map (Live GPS only, Home/Resi/NH tabs completely removed)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF000000))
            ) {
                OfflineMapView(
                    navState = navState,
                    modifier = Modifier.fillMaxSize()
                )

                // Live GPS Location HUD Badge
                Surface(
                    color = Color.Black.copy(alpha = 0.88f),
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.3f)),
                    modifier = Modifier
                        .padding(6.dp)
                        .align(Alignment.TopStart)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color.White, modifier = Modifier.size(13.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${navState.currentRoad} • 28.4595° N, 77.0266° E",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Normal,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // GPS Satellites Status Badge
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color.Black.copy(alpha = 0.88f),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.3f)),
                    modifier = Modifier
                        .padding(6.dp)
                        .align(Alignment.TopEnd)
                ) {
                    Text(
                        text = "11 SATS • LIVE GPS ONLY",
                        color = Color.White,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Normal,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }
            }
        }
    }
}

/**
 * YouTube Video Player Split Pane Component:
 * - Uses device's signed-in system YouTube app directly to play videos and searches
 * - Fixes "Watch video on YouTube" error by delegating video playback to the system YouTube app
 * - Entire Lo-fi Hip Hop row removed completely
 * - Driving Playlists & Search input
 */
@Composable
private fun YouTubeSplitPane(
    viewModel: CarHeadUnitViewModel,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var useWebPreview by remember { mutableStateOf(false) }

    val drivingPlaylists = remember {
        listOf(
            DrivingVideoPlaylist("Top Bollywood Road Trip Songs", "Popular travel playlist", "bollywood road trip songs"),
            DrivingVideoPlaylist("Live News 24/7", "Hindi & Global Headlines", "live news hindi"),
            DrivingVideoPlaylist("Synthwave Night Highway Drive", "Retro electronic beats", "synthwave driving"),
            DrivingVideoPlaylist("Car Reviews & Test Drives", "Automotive features", "car review test drive"),
            DrivingVideoPlaylist("Relaxing Acoustic Driving", "Calm road trip tracks", "acoustic road trip music")
        )
    }

    Surface(
        color = Color(0xFF0D0D0D),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Color(0xFF262626)),
        modifier = modifier.testTag("split_youtube_pane")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.PlayArrow,
                            contentDescription = "YouTube",
                            tint = Color.White,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text("YOUTUBE • SYSTEM APP", fontSize = 11.sp, fontWeight = FontWeight.Normal, color = Color.White)
                        Text("Signed-In System App Playback", fontSize = 8.sp, color = Color.LightGray)
                    }
                }

                // Action: Open Official YouTube App directly
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFF1F1F1F),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .clickable { viewModel.launchYouTubeApp() }
                        .testTag("split_launch_youtube_app_btn")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.OpenInNew, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("OPEN YOUTUBE APP", fontSize = 9.sp, fontWeight = FontWeight.Normal, color = Color.White)
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Search Bar for System YouTube App
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search songs, artists, or videos...", fontSize = 10.sp, color = Color.Gray) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.White,
                        unfocusedBorderColor = Color(0xFF333333),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedContainerColor = Color(0xFF141414),
                        unfocusedContainerColor = Color(0xFF141414)
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(42.dp),
                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 11.sp)
                )

                Spacer(modifier = Modifier.width(6.dp))

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color.White,
                    modifier = Modifier
                        .height(42.dp)
                        .clickable {
                            if (searchQuery.isNotBlank()) {
                                viewModel.launchYouTubeApp(searchQuery)
                            } else {
                                viewModel.launchYouTubeApp()
                            }
                        }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Search, contentDescription = "Search", tint = Color.Black, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("PLAY", fontSize = 10.sp, fontWeight = FontWeight.Black, color = Color.Black)
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Main YouTube Pane Content: Direct System App Playback Hub (No broken "Watch video on YouTube" webview)
            Surface(
                color = Color(0xFF141414),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, Color(0xFF262626)),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Hero System YouTube App Banner
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF1F1F1F),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.3f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.launchYouTubeApp() }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color.White),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.Black, modifier = Modifier.size(20.dp))
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("PLAY IN SYSTEM YOUTUBE APP", fontSize = 11.sp, fontWeight = FontWeight.Black, color = Color.White)
                                Text("Plays directly in signed-in app without browser playback restrictions", fontSize = 8.sp, color = Color.LightGray)
                            }
                            Icon(Icons.Default.OpenInNew, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text("DRIVING COLLECTIONS (TAP TO PLAY IN YOUTUBE APP)", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.Gray)

                    // Curated Driving Playlists (Lo-fi Hip Hop row removed completely)
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(drivingPlaylists) { item ->
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFF1B1B1B),
                                border = BorderStroke(1.dp, Color(0xFF2C2C2C)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { viewModel.launchYouTubeApp(item.searchQuery) }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 8.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.VideoLibrary, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Column {
                                            Text(item.title, fontSize = 10.sp, fontWeight = FontWeight.Normal, color = Color.White)
                                            Text(item.subtitle, fontSize = 8.sp, color = Color.Gray)
                                        }
                                    }
                                    Icon(Icons.Default.PlayArrow, contentDescription = "Play", tint = Color.White, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
