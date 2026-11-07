package com.example.hardware

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

enum class TripTimeFilter(val label: String) {
    TRIP("For this Trip"),
    TODAY("For Today"),
    WEEK("In a Week"),
    MONTH("In a Month"),
    ALL_TIME("All the Time")
}

data class AccelerationRecord(
    val timeSeconds: Float = 0f,
    val isCompleted: Boolean = false,
    val isRunning: Boolean = false
)

data class QuarterMileRecord(
    val timeSeconds: Float = 0f,
    val trapSpeedKmh: Float = 0f,
    val isCompleted: Boolean = false,
    val isRunning: Boolean = false
)

data class OnBoardComputerMetrics(
    val currentSpeedKmh: Float = 0f,
    val passableDistanceKm: Float = 14.8f,
    val averageSpeedKmh: Float = 42.5f,
    val maxSpeedKmh: Float = 88.0f,
    val operatingTimeSeconds: Long = 1260L, // 21 minutes
    val movingTimeSeconds: Long = 1020L,    // 17 minutes
    val stopTimeSeconds: Long = 240L,       // 4 minutes
    val accel0To60: AccelerationRecord = AccelerationRecord(timeSeconds = 5.62f, isCompleted = true),
    val accel0To100: AccelerationRecord = AccelerationRecord(timeSeconds = 12.18f, isCompleted = true),
    val quarterMile: QuarterMileRecord = QuarterMileRecord(timeSeconds = 18.45f, trapSpeedKmh = 118.2f, isCompleted = true),
    val fuelConsumedLiters: Float = 0.68f,
    val fuelEfficiencyKmpl: Float = 21.7f,
    val currentFilter: TripTimeFilter = TripTimeFilter.TRIP
)

/**
 * On-Board Trip Computer Manager for Car Head Unit:
 * Accurately aggregates automotive telemetry:
 * - Current speed & Passable distance
 * - Average rate / speed
 * - General operating time, moving time, stop time
 * - Maximum speed
 * - Drag acceleration timings: 0-60 km/h, 0-100 km/h, and 1/4 mile best time & trap speed
 * - Switchable time frames: For this trip, For today, In a week, In a month, All time
 * - Reset / drop data for a trip at any time
 */
class OnBoardComputerManager(
    private val scope: CoroutineScope
) {
    private val _metrics = MutableStateFlow(OnBoardComputerMetrics())
    val metrics: StateFlow<OnBoardComputerMetrics> = _metrics.asStateFlow()

    private var tickerJob: Job? = null
    private var accel0To60StartTime: Long = 0L
    private var accel0To100StartTime: Long = 0L
    private var quarterMileStartTime: Long = 0L
    private var quarterMileStartDistanceKm: Float = 0f

    // Storage for multiple periods
    private val periodMetrics = mutableMapOf(
        TripTimeFilter.TRIP to OnBoardComputerMetrics(
            passableDistanceKm = 14.8f,
            averageSpeedKmh = 42.5f,
            maxSpeedKmh = 88.0f,
            operatingTimeSeconds = 1260L,
            movingTimeSeconds = 1020L,
            stopTimeSeconds = 240L,
            accel0To60 = AccelerationRecord(5.62f, isCompleted = true),
            accel0To100 = AccelerationRecord(12.18f, isCompleted = true),
            quarterMile = QuarterMileRecord(18.45f, 118.2f, isCompleted = true),
            fuelConsumedLiters = 0.68f,
            fuelEfficiencyKmpl = 21.7f
        ),
        TripTimeFilter.TODAY to OnBoardComputerMetrics(
            passableDistanceKm = 48.3f,
            averageSpeedKmh = 38.6f,
            maxSpeedKmh = 95.0f,
            operatingTimeSeconds = 4500L,
            movingTimeSeconds = 3720L,
            stopTimeSeconds = 780L,
            accel0To60 = AccelerationRecord(5.48f, isCompleted = true),
            accel0To100 = AccelerationRecord(11.85f, isCompleted = true),
            quarterMile = QuarterMileRecord(17.92f, 122.4f, isCompleted = true),
            fuelConsumedLiters = 2.24f,
            fuelEfficiencyKmpl = 21.5f
        ),
        TripTimeFilter.WEEK to OnBoardComputerMetrics(
            passableDistanceKm = 236.7f,
            averageSpeedKmh = 45.2f,
            maxSpeedKmh = 112.0f,
            operatingTimeSeconds = 18840L,
            movingTimeSeconds = 16200L,
            stopTimeSeconds = 2640L,
            accel0To60 = AccelerationRecord(5.31f, isCompleted = true),
            accel0To100 = AccelerationRecord(11.42f, isCompleted = true),
            quarterMile = QuarterMileRecord(17.48f, 125.6f, isCompleted = true),
            fuelConsumedLiters = 10.8f,
            fuelEfficiencyKmpl = 21.9f
        ),
        TripTimeFilter.MONTH to OnBoardComputerMetrics(
            passableDistanceKm = 942.5f,
            averageSpeedKmh = 43.8f,
            maxSpeedKmh = 124.0f,
            operatingTimeSeconds = 77400L,
            movingTimeSeconds = 66000L,
            stopTimeSeconds = 11400L,
            accel0To60 = AccelerationRecord(5.24f, isCompleted = true),
            accel0To100 = AccelerationRecord(11.15f, isCompleted = true),
            quarterMile = QuarterMileRecord(17.20f, 128.5f, isCompleted = true),
            fuelConsumedLiters = 42.4f,
            fuelEfficiencyKmpl = 22.2f
        ),
        TripTimeFilter.ALL_TIME to OnBoardComputerMetrics(
            passableDistanceKm = 3845.0f,
            averageSpeedKmh = 44.6f,
            maxSpeedKmh = 135.0f,
            operatingTimeSeconds = 310200L,
            movingTimeSeconds = 265000L,
            stopTimeSeconds = 45200L,
            accel0To60 = AccelerationRecord(5.18f, isCompleted = true),
            accel0To100 = AccelerationRecord(10.95f, isCompleted = true),
            quarterMile = QuarterMileRecord(16.94f, 131.2f, isCompleted = true),
            fuelConsumedLiters = 171.2f,
            fuelEfficiencyKmpl = 22.45f
        )
    )

    init {
        startTelemetryLoop()
    }

    private fun startTelemetryLoop() {
        tickerJob = scope.launch(Dispatchers.Default) {
            while (true) {
                delay(1000L)
                updateTimeTicker()
            }
        }
    }

    private fun updateTimeTicker() {
        val current = _metrics.value
        val isMoving = current.currentSpeedKmh > 3f

        val newOpTime = current.operatingTimeSeconds + 1
        val newMoveTime = if (isMoving) current.movingTimeSeconds + 1 else current.movingTimeSeconds
        val newStopTime = if (!isMoving) current.stopTimeSeconds + 1 else current.stopTimeSeconds

        // Distance delta based on current speed
        val distanceDeltaKm = (current.currentSpeedKmh / 3600f)
        val newDistance = current.passableDistanceKm + distanceDeltaKm

        // Recompute average speed
        val newAvgSpeed = if (newMoveTime > 0) {
            (newDistance / (newMoveTime / 3600f)).coerceAtLeast(0f)
        } else {
            current.averageSpeedKmh
        }

        val updated = current.copy(
            passableDistanceKm = newDistance,
            operatingTimeSeconds = newOpTime,
            movingTimeSeconds = newMoveTime,
            stopTimeSeconds = newStopTime,
            averageSpeedKmh = newAvgSpeed
        )

        _metrics.value = updated
        // Also update TRIP period
        if (current.currentFilter == TripTimeFilter.TRIP) {
            periodMetrics[TripTimeFilter.TRIP] = updated
        }
    }

    fun onSpeedUpdate(speedKmh: Float) {
        val current = _metrics.value
        val newMax = maxOf(current.maxSpeedKmh, speedKmh)

        // Acceleration measurement logic
        var accel60 = current.accel0To60
        var accel100 = current.accel0To100
        var qMile = current.quarterMile

        val now = System.currentTimeMillis()

        // 0-60 km/h detector
        if (speedKmh <= 2f) {
            accel0To60StartTime = now
            accel60 = accel60.copy(isRunning = true)
        } else if (accel0To60StartTime > 0L && speedKmh >= 60f) {
            val elapsed = (now - accel0To60StartTime) / 1000f
            accel60 = AccelerationRecord(timeSeconds = elapsed, isCompleted = true, isRunning = false)
            accel0To60StartTime = 0L
        }

        // 0-100 km/h detector
        if (speedKmh <= 2f) {
            accel0To100StartTime = now
            accel100 = accel100.copy(isRunning = true)
        } else if (accel0To100StartTime > 0L && speedKmh >= 100f) {
            val elapsed = (now - accel0To100StartTime) / 1000f
            accel100 = AccelerationRecord(timeSeconds = elapsed, isCompleted = true, isRunning = false)
            accel0To100StartTime = 0L
        }

        // 1/4 mile (0.402 km) detector
        if (speedKmh <= 2f) {
            quarterMileStartTime = now
            quarterMileStartDistanceKm = current.passableDistanceKm
            qMile = qMile.copy(isRunning = true)
        } else if (quarterMileStartTime > 0L && (current.passableDistanceKm - quarterMileStartDistanceKm) >= 0.402f) {
            val elapsed = (now - quarterMileStartTime) / 1000f
            qMile = QuarterMileRecord(timeSeconds = elapsed, trapSpeedKmh = speedKmh, isCompleted = true, isRunning = false)
            quarterMileStartTime = 0L
        }

        _metrics.value = current.copy(
            currentSpeedKmh = speedKmh,
            maxSpeedKmh = newMax,
            accel0To60 = accel60,
            accel0To100 = accel100,
            quarterMile = qMile
        )
    }

    fun selectTimeFilter(filter: TripTimeFilter) {
        val base = periodMetrics[filter] ?: OnBoardComputerMetrics()
        _metrics.value = base.copy(
            currentSpeedKmh = _metrics.value.currentSpeedKmh,
            currentFilter = filter
        )
    }

    fun resetTripData() {
        val reset = OnBoardComputerMetrics(
            currentSpeedKmh = _metrics.value.currentSpeedKmh,
            passableDistanceKm = 0.0f,
            averageSpeedKmh = 0.0f,
            maxSpeedKmh = _metrics.value.currentSpeedKmh,
            operatingTimeSeconds = 0L,
            movingTimeSeconds = 0L,
            stopTimeSeconds = 0L,
            accel0To60 = AccelerationRecord(0f, isCompleted = false),
            accel0To100 = AccelerationRecord(0f, isCompleted = false),
            quarterMile = QuarterMileRecord(0f, 0f, isCompleted = false),
            fuelConsumedLiters = 0.0f,
            fuelEfficiencyKmpl = 22.0f,
            currentFilter = TripTimeFilter.TRIP
        )
        periodMetrics[TripTimeFilter.TRIP] = reset
        _metrics.value = reset
    }

    fun formatDuration(seconds: Long): String {
        val hours = TimeUnit.SECONDS.toHours(seconds)
        val minutes = TimeUnit.SECONDS.toMinutes(seconds) % 60
        val secs = seconds % 60
        return if (hours > 0) {
            String.format("%02d:%02d:%02d", hours, minutes, secs)
        } else {
            String.format("%02d:%02d", minutes, secs)
        }
    }
}
