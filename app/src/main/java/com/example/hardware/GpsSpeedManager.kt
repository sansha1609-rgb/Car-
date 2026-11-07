package com.example.hardware

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class GpsSpeedManager(
    private val context: Context,
    private val onSpeedChanged: (Int) -> Unit
) : LocationListener {

    private val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
    private val _currentSpeedKmh = MutableStateFlow(54)
    val currentSpeedKmh: StateFlow<Int> = _currentSpeedKmh.asStateFlow()

    private val _hasGpsFix = MutableStateFlow(false)
    val hasGpsFix: StateFlow<Boolean> = _hasGpsFix.asStateFlow()

    @SuppressLint("MissingPermission")
    fun startGpsUpdates() {
        try {
            if (locationManager?.isProviderEnabled(LocationManager.GPS_PROVIDER) == true) {
                locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    1000L,
                    1f,
                    this
                )
            }
        } catch (_: SecurityException) {
            // Location permission not yet granted; UI allows manual simulation and requests permission
        }
    }

    fun stopGpsUpdates() {
        try {
            locationManager?.removeUpdates(this)
        } catch (_: SecurityException) {}
    }

    override fun onLocationChanged(location: Location) {
        _hasGpsFix.value = true
        if (location.hasSpeed()) {
            val kmh = (location.speed * 3.6f).toInt()
            _currentSpeedKmh.value = kmh
            onSpeedChanged(kmh)
        }
    }

    fun setSimulatedSpeed(speed: Int) {
        _currentSpeedKmh.value = speed
        onSpeedChanged(speed)
    }

    @Deprecated("Deprecated in Java")
    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
    override fun onProviderEnabled(provider: String) { _hasGpsFix.value = true }
    override fun onProviderDisabled(provider: String) { _hasGpsFix.value = false }
}
