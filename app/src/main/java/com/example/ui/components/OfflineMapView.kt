package com.example.ui.components

import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.navigation.NavigationState

@Composable
fun OfflineMapView(
    navState: NavigationState,
    modifier: Modifier = Modifier,
    onMapClick: () -> Unit = {}
) {
    var zoomLevel by remember { mutableFloatStateOf(1.0f) }
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary

    Box(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
            .testTag("offline_map_view")
    ) {
        // High-Contrast Vector Map Canvas
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .clickable { onMapClick() }
        ) {
            val w = size.width
            val h = size.height

            // Secondary Road Network Grid
            val gridColor = Color(0xFF1E293B).copy(alpha = 0.7f)
            val strokeGrid = 2.dp.toPx()

            // Horizontal secondary streets
            for (i in 1..5) {
                val y = h * (i / 6f)
                drawLine(
                    color = gridColor,
                    start = Offset(0f, y),
                    end = Offset(w, y),
                    strokeWidth = strokeGrid
                )
            }

            // Vertical streets
            for (i in 1..6) {
                val x = w * (i / 7f)
                drawLine(
                    color = gridColor,
                    start = Offset(x, 0f),
                    end = Offset(x, h),
                    strokeWidth = strokeGrid
                )
            }

            // Major Arterial Highway (Curved Path)
            val highwayPath = Path().apply {
                moveTo(w * 0.05f, h * 0.85f)
                cubicTo(
                    w * 0.35f, h * 0.80f,
                    w * 0.40f, h * 0.40f,
                    w * 0.95f, h * 0.20f
                )
            }
            // Highway outline
            drawPath(
                path = highwayPath,
                color = Color(0xFF334155),
                style = Stroke(width = 18.dp.toPx() * zoomLevel, cap = StrokeCap.Round, join = StrokeJoin.Round)
            )

            // Active GPS Guided Route Polyline (Glowing Primary Color)
            if (navState.isNavigating) {
                val routePath = Path().apply {
                    moveTo(w * 0.20f, h * 0.75f)
                    cubicTo(
                        w * 0.35f, h * 0.70f,
                        w * 0.45f, h * 0.45f,
                        w * 0.80f, h * 0.28f
                    )
                }

                // Route glow
                drawPath(
                    path = routePath,
                    color = primaryColor.copy(alpha = 0.35f),
                    style = Stroke(width = 16.dp.toPx() * zoomLevel, cap = StrokeCap.Round)
                )

                // Route main vector
                drawPath(
                    path = routePath,
                    color = primaryColor,
                    style = Stroke(width = 8.dp.toPx() * zoomLevel, cap = StrokeCap.Round)
                )

                // Destination Pin
                val destX = w * 0.80f
                val destY = h * 0.28f
                drawCircle(
                    color = Color.Red,
                    radius = 9.dp.toPx(),
                    center = Offset(destX, destY)
                )
                drawCircle(
                    color = Color.White,
                    radius = 4.dp.toPx(),
                    center = Offset(destX, destY)
                )
            }

            // Current Vehicle Marker (Maruti Celerio GPS Position)
            val carX = w * 0.36f
            val carY = h * 0.65f

            // Radar pulse ring around car
            drawCircle(
                color = secondaryColor.copy(alpha = 0.25f),
                radius = 24.dp.toPx(),
                center = Offset(carX, carY)
            )
            drawCircle(
                color = primaryColor,
                radius = 11.dp.toPx(),
                center = Offset(carX, carY)
            )
            drawCircle(
                color = Color.White,
                radius = 5.dp.toPx(),
                center = Offset(carX, carY)
            )

            // Speed camera icon location on road
            if (navState.incidentsAhead.isNotEmpty()) {
                val cameraX = w * 0.52f
                val cameraY = h * 0.44f
                drawCircle(
                    color = Color(0xFFFF9100),
                    radius = 8.dp.toPx(),
                    center = Offset(cameraX, cameraY)
                )
            }
        }

        // Top Status Header: Offline Cache status & GPS satellites
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = Color.Black.copy(alpha = 0.75f),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CloudDone,
                        contentDescription = "Offline Cache",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "OFFLINE CACHE LOCKED",
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Surface(
                color = Color.Black.copy(alpha = 0.75f),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = "GPS: 11 SATS",
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }

        // Floating Map Controls (Oversized touch targets for car drivers)
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surface,
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier
                    .size(44.dp)
                    .clickable { zoomLevel = (zoomLevel + 0.2f).coerceAtMost(2.0f) }
                    .testTag("map_zoom_in")
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Zoom In",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surface,
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier
                    .size(44.dp)
                    .clickable { zoomLevel = (zoomLevel - 0.2f).coerceAtLeast(0.6f) }
                    .testTag("map_zoom_out")
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Remove,
                        contentDescription = "Zoom Out",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .size(44.dp)
                    .clickable { /* Re-center on vehicle */ }
                    .testTag("map_recenter")
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.MyLocation,
                        contentDescription = "Re-center vehicle",
                        tint = Color.Black
                    )
                }
            }
        }
    }
}
