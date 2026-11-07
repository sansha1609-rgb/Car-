package com.example.launcher

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

class AppsManager(private val context: Context) {

    private val _installedApps = MutableStateFlow<List<AppInfo>>(emptyList())
    val installedApps: StateFlow<List<AppInfo>> = _installedApps.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    suspend fun loadInstalledApps() {
        _isLoading.value = true
        withContext(Dispatchers.IO) {
            val pm = context.packageManager
            val intent = Intent(Intent.ACTION_MAIN, null).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
            }

            val resolveInfos = try {
                pm.queryIntentActivities(intent, 0)
            } catch (e: Exception) {
                Log.e("AppsManager", "Error querying launcher activities", e)
                emptyList()
            }

            val appList = mutableListOf<AppInfo>()
            for (ri in resolveInfos) {
                try {
                    val pkgName = ri.activityInfo.packageName
                    // Skip our own launcher from the app drawer grid to avoid recursive launcher nesting
                    if (pkgName == context.packageName) continue

                    val label = ri.loadLabel(pm)?.toString() ?: ri.activityInfo.name
                    val icon = ri.loadIcon(pm)
                    val isSystem = (ri.activityInfo.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0

                    val category = categorizeApp(pkgName, label)

                    appList.add(
                        AppInfo(
                            label = label,
                            packageName = pkgName,
                            activityName = ri.activityInfo.name,
                            icon = icon,
                            category = category,
                            isSystemApp = isSystem
                        )
                    )
                } catch (e: Exception) {
                    Log.w("AppsManager", "Failed to load app item", e)
                }
            }

            if (appList.size < 8) {
                val defaults = listOf(
                    AppInfo("Google Maps", "com.google.android.apps.maps", "com.google.android.maps.MapsActivity", category = AppCategory.NAVIGATION),
                    AppInfo("Spotify", "com.spotify.music", "com.spotify.music.MainActivity", category = AppCategory.MEDIA),
                    AppInfo("YouTube Music", "com.google.android.apps.youtube.music", "com.google.android.apps.youtube.music.activities.MusicActivity", category = AppCategory.MEDIA),
                    AppInfo("FM Radio", "com.android.fmradio", "com.android.fmradio.FmMainActivity", category = AppCategory.MEDIA),
                    AppInfo("Phone Dialer", "com.android.dialer", "com.android.dialer.main.impl.MainActivity", category = AppCategory.COMMUNICATION),
                    AppInfo("Contacts", "com.android.contacts", "com.android.contacts.activities.PeopleActivity", category = AppCategory.COMMUNICATION),
                    AppInfo("System Settings", "com.android.settings", "com.android.settings.Settings", category = AppCategory.SYSTEM),
                    AppInfo("Web Browser", "com.android.chrome", "com.google.android.apps.chrome.Main", category = AppCategory.SYSTEM),
                    AppInfo("Clock & Alarms", "com.android.deskclock", "com.android.deskclock.DeskClock", category = AppCategory.SYSTEM),
                    AppInfo("OBD Diagnostics", "com.example.celerio.diagnostics", "com.example.DiagnosticsActivity", category = AppCategory.SYSTEM),
                    AppInfo("Trip Mileage Log", "com.example.celerio.triplog", "com.example.TripLogActivity", category = AppCategory.SYSTEM),
                    AppInfo("Calculator", "com.android.calculator2", "com.android.calculator2.Calculator", category = AppCategory.SYSTEM)
                )
                for (def in defaults) {
                    if (appList.none { it.packageName == def.packageName || it.label.equals(def.label, ignoreCase = true) }) {
                        appList.add(def)
                    }
                }
            }

            // Sort alphabetically by label
            appList.sortBy { it.label.lowercase() }

            _installedApps.value = appList
            _isLoading.value = false
        }
    }

    private fun categorizeApp(packageName: String, label: String): AppCategory {
        val lowerPkg = packageName.lowercase()
        val lowerLabel = label.lowercase()

        return when {
            lowerPkg.contains("maps") || lowerPkg.contains("navigation") || lowerPkg.contains("waze") ||
                    lowerLabel.contains("map") || lowerLabel.contains("nav") || lowerLabel.contains("gps") -> AppCategory.NAVIGATION

            lowerPkg.contains("music") || lowerPkg.contains("audio") || lowerPkg.contains("spotify") ||
                    lowerPkg.contains("radio") || lowerPkg.contains("media") || lowerPkg.contains("youtube") ||
                    lowerLabel.contains("music") || lowerLabel.contains("audio") || lowerLabel.contains("player") -> AppCategory.MEDIA

            lowerPkg.contains("dialer") || lowerPkg.contains("phone") || lowerPkg.contains("contact") ||
                    lowerLabel.contains("phone") || lowerLabel.contains("call") || lowerLabel.contains("contact") -> AppCategory.COMMUNICATION

            lowerPkg.contains("settings") || lowerPkg.contains("system") || lowerPkg.contains("calculator") ||
                    lowerPkg.contains("clock") || lowerPkg.contains("file") || lowerLabel.contains("setting") -> AppCategory.SYSTEM

            else -> AppCategory.ALL
        }
    }

    fun launchApp(app: AppInfo): Boolean {
        return try {
            val intent = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
                component = ComponentName(app.packageName, app.activityName)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
            }
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            Log.e("AppsManager", "Failed to launch app: ${app.packageName}", e)
            // Fallback 1: try launch intent for package
            try {
                val launchIntent = context.packageManager.getLaunchIntentForPackage(app.packageName)
                if (launchIntent != null) {
                    launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(launchIntent)
                    return true
                }
            } catch (_: Exception) {}

            // Fallback 2: Category-based intents
            when (app.category) {
                AppCategory.NAVIGATION -> launchNavigationApp()
                AppCategory.MEDIA -> launchMusicApp()
                AppCategory.COMMUNICATION -> launchDialer()
                AppCategory.SYSTEM -> launchSystemSettings()
                else -> false
            }
        }
    }

    /**
     * Launches the system navigation application (e.g. Google Maps, Waze, or any installed Geo handler)
     */
    fun launchNavigationApp(destinationQuery: String? = null): Boolean {
        val geoUri = if (!destinationQuery.isNullOrBlank()) {
            Uri.parse("geo:0,0?q=${Uri.encode(destinationQuery)}")
        } else {
            Uri.parse("geo:0,0?q=")
        }

        val navIntent = Intent(Intent.ACTION_VIEW, geoUri).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }

        // Try explicitly with Google Maps first if installed
        navIntent.setPackage("com.google.android.apps.maps")
        return try {
            context.startActivity(navIntent)
            true
        } catch (e: Exception) {
            // Fallback to any app that handles geo URI
            navIntent.setPackage(null)
            try {
                context.startActivity(navIntent)
                true
            } catch (e2: Exception) {
                // Second fallback: find any app in NAVIGATION category
                val navApp = _installedApps.value.firstOrNull { it.category == AppCategory.NAVIGATION }
                if (navApp != null) {
                    launchApp(navApp)
                } else {
                    false
                }
            }
        }
    }

    /**
     * Launches the device's music application
     */
    fun launchMusicApp(): Boolean {
        // Look for installed media app or launch generic music intent
        val mediaApp = _installedApps.value.firstOrNull {
            it.packageName.contains("spotify") || it.packageName.contains("music") || it.category == AppCategory.MEDIA
        }

        if (mediaApp != null) {
            return launchApp(mediaApp)
        }

        val intent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_APP_MUSIC)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        return try {
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Launches the device's built-in FM Radio application
     */
    fun launchFmRadio(): Boolean {
        // 1. Try finding installed radio app in package manager
        val fmApp = _installedApps.value.firstOrNull {
            it.packageName.contains("fmradio", ignoreCase = true) ||
            it.packageName.contains("radio", ignoreCase = true) ||
            it.label.contains("radio", ignoreCase = true) ||
            it.label.contains("fm", ignoreCase = true)
        }
        if (fmApp != null) {
            val launched = launchApp(fmApp)
            if (launched) return true
        }

        // 2. Try explicit packages commonly found on car head units and Android devices
        val candidatePackages = listOf(
            "com.android.fmradio",
            "com.mediatek.fmradio",
            "com.quicinc.fmradio",
            "com.microntek.radio",
            "com.syu.radio",
            "com.ts.radioplayer",
            "com.broadcom.fm",
            "com.hct.radio"
        )
        for (pkg in candidatePackages) {
            try {
                val launchIntent = context.packageManager.getLaunchIntentForPackage(pkg)
                if (launchIntent != null) {
                    launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(launchIntent)
                    return true
                }
            } catch (_: Exception) {}
        }

        // 3. Fallback: try opening radio tuner or music application
        return try {
            val intent = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_APP_MUSIC)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            true
        } catch (_: Exception) {
            false
        }
    }

    /**
     * Places a direct phone call using CALL_PHONE permission if granted, else ACTION_DIAL
     */
    fun makePhoneCall(phoneNumber: String): Boolean {
        val sanitized = phoneNumber.trim().replace(" ", "")
        if (sanitized.isBlank()) return launchDialer()

        val callIntent = Intent(Intent.ACTION_CALL, Uri.parse("tel:$sanitized")).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        return try {
            context.startActivity(callIntent)
            true
        } catch (e: Exception) {
            // Fallback to dial intent
            launchDialer(sanitized)
        }
    }

    /**
     * Launches Phone dialer
     */
    fun launchDialer(phoneNumber: String? = null): Boolean {
        val intent = if (!phoneNumber.isNullOrBlank()) {
            Intent(Intent.ACTION_DIAL, Uri.parse("tel:${phoneNumber.trim().replace(" ", "")}")).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
        } else {
            Intent(Intent.ACTION_DIAL).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
        }

        return try {
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Launches Contacts
     */
    fun launchContacts(): Boolean {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse("content://contacts/people/")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        return try {
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Launches System Settings screen
     */
    fun launchSystemSettings(action: String = Settings.ACTION_SETTINGS): Boolean {
        val intent = Intent(action).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        return try {
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            try {
                context.startActivity(Intent(Settings.ACTION_SETTINGS).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                })
                true
            } catch (e2: Exception) {
                false
            }
        }
    }

    /**
     * Opens default home launcher settings so user can set this app as default
     */
    fun promptSetDefaultHome(): Boolean {
        return launchSystemSettings(Settings.ACTION_HOME_SETTINGS)
    }
}
