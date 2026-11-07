package com.example.launcher

import android.bluetooth.BluetoothAdapter
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.LocationManager
import android.media.AudioManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import android.os.BatteryManager
import android.os.Build
import android.provider.Settings
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class TopBarStatus(
    val timeFormatted: String = "12:00",
    val timeSecondsFormatted: String = "00",
    val dateFormatted: String = "Sunday, Sep 27",
    val isWifiConnected: Boolean = true,
    val wifiSsid: String = "Car-Hotspot-5G",
    val wifiSignalLevel: Int = 3, // 0 to 4
    val isBluetoothOn: Boolean = true,
    val isBluetoothConnected: Boolean = true,
    val connectedDeviceName: String = "Connected Phone",
    val isGpsActive: Boolean = true,
    val gpsSatellites: Int = 12,
    val batteryPercent: Int = 96,
    val isCharging: Boolean = true,
    val vehicleVoltage: Float = 14.2f, // Vehicle alternator voltage
    val systemVolumePercent: Int = 75,
    val brightnessPercent: Int = 85
)

class SystemStatusManager(
    private val context: Context,
    private val scope: CoroutineScope
) {
    private val _status = MutableStateFlow(TopBarStatus())
    val status: StateFlow<TopBarStatus> = _status.asStateFlow()

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
    private val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager

    private var timeTickerJob: Job? = null

    private val batteryReceiver = object : BroadcastReceiver() {
        override fun onReceive(c: Context?, intent: Intent?) {
            if (intent?.action == Intent.ACTION_BATTERY_CHANGED) {
                val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
                val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
                val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                        status == BatteryManager.BATTERY_STATUS_FULL

                val pct = if (level >= 0 && scale > 0) (level * 100 / scale) else 95
                _status.value = _status.value.copy(
                    batteryPercent = pct,
                    isCharging = isCharging
                )
            }
        }
    }

    init {
        // Register battery monitor
        try {
            val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
            context.registerReceiver(batteryReceiver, filter)
        } catch (e: Exception) {
            Log.w("SystemStatusManager", "Could not register battery receiver", e)
        }

        startTimeTicker()
        updateNetworkAndBluetooth()
        updateVolume()
    }

    private fun startTimeTicker() {
        timeTickerJob?.cancel()
        timeTickerJob = scope.launch(Dispatchers.Default) {
            val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            val secFormat = SimpleDateFormat("ss", Locale.getDefault())
            val dateFormat = SimpleDateFormat("EEEE, d MMMM", Locale.getDefault())

            while (true) {
                val now = Date()
                val current = _status.value
                val newTime = timeFormat.format(now)
                val newSec = secFormat.format(now)
                val newDate = dateFormat.format(now)

                val gpsEnabled = try {
                    locationManager?.isProviderEnabled(LocationManager.GPS_PROVIDER) ?: true
                } catch (e: Exception) {
                    true
                }

                _status.value = current.copy(
                    timeFormatted = newTime,
                    timeSecondsFormatted = newSec,
                    dateFormatted = newDate,
                    isGpsActive = gpsEnabled
                )

                delay(1000L)
            }
        }
    }

    fun updateNetworkAndBluetooth() {
        try {
            var wifiConnected = false
            var ssid = "Vehicle Wi-Fi"

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val activeNetwork = connectivityManager?.activeNetwork
                val capabilities = connectivityManager?.getNetworkCapabilities(activeNetwork)
                wifiConnected = capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true ||
                        capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true
            } else {
                @Suppress("DEPRECATION")
                val netInfo = connectivityManager?.activeNetworkInfo
                wifiConnected = netInfo?.isConnected == true
            }

            val wifiInfo = wifiManager?.connectionInfo
            if (wifiInfo != null && wifiConnected) {
                val currentSsid = wifiInfo.ssid?.replace("\"", "")
                if (!currentSsid.isNullOrBlank() && currentSsid != "<unknown ssid>") {
                    ssid = currentSsid
                }
            }

            // Bluetooth state
            var btOn = true
            var btConnected = true
            var phoneName = "Pixel 9 Pro (Car Connected)"
            try {
                val btAdapter = BluetoothAdapter.getDefaultAdapter()
                if (btAdapter != null) {
                    btOn = btAdapter.isEnabled
                    btConnected = btOn
                    val bonded = btAdapter.bondedDevices
                    if (!bonded.isNullOrEmpty()) {
                        phoneName = bonded.first().name ?: "Phone Connected"
                    }
                }
            } catch (e: Exception) {
                // Ignore security / mock in container
            }

            _status.value = _status.value.copy(
                isWifiConnected = wifiConnected,
                wifiSsid = ssid,
                isBluetoothOn = btOn,
                isBluetoothConnected = btConnected,
                connectedDeviceName = phoneName
            )
        } catch (e: Exception) {
            Log.w("SystemStatusManager", "Network update error", e)
        }
    }

    fun updateVolume() {
        try {
            val maxVol = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
            val currentVol = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
            val pct = if (maxVol > 0) (currentVol * 100 / maxVol) else 75
            _status.value = _status.value.copy(systemVolumePercent = pct)
        } catch (e: Exception) {
            // Safe fallback
        }
    }

    fun setSystemVolume(percent: Int) {
        try {
            val clamped = percent.coerceIn(0, 100)
            val maxVol = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
            val target = (clamped * maxVol) / 100
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, target, 0)
            _status.value = _status.value.copy(systemVolumePercent = clamped)
        } catch (e: Exception) {
            _status.value = _status.value.copy(systemVolumePercent = percent.coerceIn(0, 100))
        }
    }

    fun setBrightness(percent: Int) {
        val clamped = percent.coerceIn(10, 100)
        _status.value = _status.value.copy(brightnessPercent = clamped)
    }

    fun destroy() {
        timeTickerJob?.cancel()
        try {
            context.unregisterReceiver(batteryReceiver)
        } catch (_: Exception) {}
    }
}
