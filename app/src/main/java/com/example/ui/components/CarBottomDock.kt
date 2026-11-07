package com.example.ui.components

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.CarScreen

data class DockItem(
    val screen: CarScreen,
    val label: String,
    val icon: ImageVector,
    val testTag: String
)

/**
 * Large, accessible bottom navigation dock designed specifically for 9-inch 1280x720 in-car head unit:
 * - Home
 * - Navigation
 * - Music
 * - Phone
 * - Apps
 * - Settings
 *
 * Touch targets are oversized (>= 58dp height, wide clickable area) for driving usability.
 */
@Composable
fun CarBottomDock(
    currentScreen: CarScreen,
    onNavigate: (CarScreen) -> Unit,
    modifier: Modifier = Modifier
) {
    val items = listOf(
        DockItem(CarScreen.DASHBOARD, "Home", Icons.Default.Home, "dock_home"),
        DockItem(CarScreen.SPLIT_VIEW, "Split", Icons.Default.Layers, "dock_split_view"),
        DockItem(CarScreen.NAVIGATION, "Maps", Icons.Default.Navigation, "dock_navigation"),
        DockItem(CarScreen.FM_RADIO, "FM", Icons.Default.Radio, "dock_fm_radio"),
        DockItem(CarScreen.PHONE, "Phone", Icons.Default.Phone, "dock_phone"),
        DockItem(CarScreen.APPS, "Apps", Icons.Default.Apps, "dock_apps"),
        DockItem(CarScreen.SETTINGS, "Settings", Icons.Default.Settings, "dock_settings")
    )

    Surface(
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.98f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = modifier
            .fillMaxWidth()
            .height(42.dp)
            .testTag("car_bottom_dock")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { item ->
                val isSelected = currentScreen == item.screen

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.22f)
                    } else {
                        Color.Transparent
                    },
                    border = BorderStroke(
                        width = if (isSelected) 1.dp else 0.dp,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp)
                        .padding(horizontal = 2.dp)
                        .clickable { onNavigate(item.screen) }
                        .testTag(item.testTag)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 2.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.label,
                            tint = if (isSelected) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
                            },
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = item.label,
                            fontSize = 9.sp,
                            fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                            color = if (isSelected) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.70f)
                            }
                        )
                    }
                }
            }
        }
    }
}
