package com.example.launcher

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class AppSortOrder(val label: String) {
    BY_NAME("Name (A-Z)"),
    BY_INSTALL_DATE("Install Date"),
    BY_UPDATE_DATE("Update Date"),
    BY_USAGE("Most Used")
}

data class QuickAppsState(
    val selectedApps: List<QuickAppSlot> = emptyList(),
    val folders: List<AppFolder> = emptyList(),
    val isEditMode: Boolean = false,
    val activeFolder: AppFolder? = null,
    val launcherTheme: LauncherScreenTheme = LauncherScreenTheme.COCKPIT_MODERN,
    val carLogo: CarBrandLogo = CarBrandLogo.SUZUKI,
    val textColorGamma: TextColorGamma = TextColorGamma.PURE_WHITE,
    val infiniteScroll: Boolean = true,
    val gridColumns: Int = 6,
    val bendSide: Boolean = false,
    val flexAngle: Float = 0f,
    val weather: WeatherInfo = WeatherInfo(),
    val address: AddressInfo = AddressInfo(),
    val appSortOrder: AppSortOrder = AppSortOrder.BY_NAME
)

class QuickAppsManager(private val context: Context) {

    private val _state = MutableStateFlow(
        QuickAppsState(
            selectedApps = listOf(
                QuickAppSlot("1", "com.google.android.apps.maps", "com.google.android.maps.MapsActivity", "Google Maps"),
                QuickAppSlot("2", "com.google.android.youtube", "com.google.android.youtube.HomeActivity", "YouTube"),
                QuickAppSlot("3", "com.spotify.music", "com.spotify.music.MainActivity", "Spotify"),
                QuickAppSlot("4", "com.google.android.apps.youtube.music", "com.google.android.apps.youtube.music.activities.MusicActivity", "YT Music"),
                QuickAppSlot("5", "com.android.fmradio", "com.android.fmradio.FmMainActivity", "Radio"),
                QuickAppSlot("6", "com.android.dialer", "com.android.dialer.main.impl.MainActivity", "Phone"),
                QuickAppSlot("7", "com.android.chrome", "com.google.android.apps.chrome.Main", "Browser"),
                QuickAppSlot("8", "com.android.settings", "com.android.settings.Settings", "Settings")
            ),
            folders = listOf(
                AppFolder(
                    id = "folder_nav",
                    name = "Navigation",
                    color = androidx.compose.ui.graphics.Color(0xFF00E676),
                    appPackages = listOf("com.google.android.apps.maps", "com.waze", "com.here.app.maps")
                ),
                AppFolder(
                    id = "folder_media",
                    name = "Music & Media",
                    color = androidx.compose.ui.graphics.Color(0xFFFF0000),
                    appPackages = listOf("com.spotify.music", "com.google.android.apps.youtube.music", "com.google.android.youtube", "com.android.fmradio")
                ),
                AppFolder(
                    id = "folder_tools",
                    name = "Car Tools",
                    color = androidx.compose.ui.graphics.Color(0xFF2979FF),
                    appPackages = listOf("com.android.settings", "com.example.celerio.diagnostics", "com.android.deskclock", "com.android.calculator2")
                )
            )
        )
    )
    val state: StateFlow<QuickAppsState> = _state.asStateFlow()

    fun setEditMode(enabled: Boolean) {
        _state.value = _state.value.copy(isEditMode = enabled)
    }

    fun setLauncherTheme(theme: LauncherScreenTheme) {
        _state.value = _state.value.copy(launcherTheme = theme)
    }

    fun setCarLogo(logo: CarBrandLogo) {
        _state.value = _state.value.copy(carLogo = logo)
    }

    fun setTextColorGamma(gamma: TextColorGamma) {
        _state.value = _state.value.copy(textColorGamma = gamma)
    }

    fun setInfiniteScroll(enabled: Boolean) {
        _state.value = _state.value.copy(infiniteScroll = enabled)
    }

    fun setGridColumns(cols: Int) {
        _state.value = _state.value.copy(gridColumns = cols.coerceIn(4, 10))
    }

    fun setBendSide(enabled: Boolean) {
        _state.value = _state.value.copy(bendSide = enabled)
    }

    fun setFlexAngle(angle: Float) {
        _state.value = _state.value.copy(flexAngle = angle.coerceIn(0f, 30f))
    }

    fun setAppSortOrder(sort: AppSortOrder) {
        _state.value = _state.value.copy(appSortOrder = sort)
    }

    fun openFolder(folder: AppFolder?) {
        _state.value = _state.value.copy(activeFolder = folder)
    }

    fun addAppToHome(appInfo: AppInfo) {
        val currentList = _state.value.selectedApps.toMutableList()
        if (currentList.none { it.appPackage == appInfo.packageName }) {
            currentList.add(
                QuickAppSlot(
                    id = "slot_${System.currentTimeMillis()}",
                    appPackage = appInfo.packageName,
                    appActivity = appInfo.activityName,
                    label = appInfo.label
                )
            )
            _state.value = _state.value.copy(selectedApps = currentList)
        }
    }

    fun removeAppFromHome(slotId: String) {
        val currentList = _state.value.selectedApps.filter { it.id != slotId }
        _state.value = _state.value.copy(selectedApps = currentList)
    }

    fun addAppToFolder(folderId: String, packageName: String) {
        val folders = _state.value.folders.map { folder ->
            if (folder.id == folderId) {
                val updatedPkgs = if (!folder.appPackages.contains(packageName)) {
                    folder.appPackages + packageName
                } else folder.appPackages
                folder.copy(appPackages = updatedPkgs)
            } else folder
        }
        _state.value = _state.value.copy(folders = folders)
    }

    fun createFolder(name: String, initialPackages: List<String> = emptyList()) {
        val newFolder = AppFolder(
            id = "folder_${System.currentTimeMillis()}",
            name = name.ifBlank { "New Folder" },
            appPackages = initialPackages
        )
        _state.value = _state.value.copy(folders = _state.value.folders + newFolder)
    }

    fun requestUninstallApp(packageName: String) {
        try {
            val intent = Intent(Intent.ACTION_DELETE).apply {
                data = Uri.parse("package:$packageName")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e("QuickAppsManager", "Error requesting app uninstall", e)
            // Fallback: open application details in system settings
            openAppDetails(packageName)
        }
    }

    fun openAppDetails(packageName: String) {
        try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.parse("package:$packageName")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e("QuickAppsManager", "Error opening app details", e)
        }
    }

    fun openDefaultHomeSettings() {
        try {
            val intent = Intent(Settings.ACTION_HOME_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (_: Exception) {
            try {
                val intent = Intent(Settings.ACTION_SETTINGS).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
            } catch (_: Exception) {}
        }
    }
}
