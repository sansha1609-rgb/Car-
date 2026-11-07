package com.example.hardware

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.random.Random

data class TireData(
    val position: String,
    val pressurePsi: Float,
    val tempCelsius: Int,
    val isWarning: Boolean = false
)

data class VehicleTelemetry(
    val vehicleModel: String = "Maruti Suzuki Celerio K10C DualJet",
    val engineRpm: Int = 1850,
    val coolantTempC: Int = 89,
    val batteryVoltage: Float = 14.1f,
    val instantEfficiencyKmpl: Float = 23.6f,
    val throttlePercent: Int = 22,
    val fuelLevelPercent: Int = 74,
    val fuelRemainingLiters: Float = 23.7f,
    val totalOdometerKm: Int = 8452,
    val engineHealthScore: Int = 96,
    val isCheckEngineLit: Boolean = false,
    val isScanning: Boolean = false,
    val tires: List<TireData> = listOf(
        TireData("Front Left", 33.2f, 34),
        TireData("Front Right", 33.0f, 35),
        TireData("Rear Left", 32.5f, 33),
        TireData("Rear Right", 33.1f, 34)
    )
)

class ObdDiagnosticsManager(
    private val scope: CoroutineScope
) {
    private val _telemetry = MutableStateFlow(VehicleTelemetry())
    val telemetry: StateFlow<VehicleTelemetry> = _telemetry.asStateFlow()

    private var telemetryJob: Job? = null

    init {
        startTelemetryLoop()
    }

    private fun startTelemetryLoop() {
        telemetryJob?.cancel()
        telemetryJob = scope.launch(Dispatchers.Default) {
            while (true) {
                delay(1200L)
                val current = _telemetry.value
                val rpmJitter = Random.nextInt(-40, 45)
                val throttleJitter = Random.nextInt(-2, 3)
                val voltJitter = (Random.nextInt(-5, 6) / 100f)
                val effJitter = (Random.nextInt(-3, 4) / 10f)

                _telemetry.value = current.copy(
                    engineRpm = (current.engineRpm + rpmJitter).coerceIn(800, 4500),
                    throttlePercent = (current.throttlePercent + throttleJitter).coerceIn(10, 60),
                    batteryVoltage = ((current.batteryVoltage + voltJitter) * 100).toInt() / 100f,
                    instantEfficiencyKmpl = ((current.instantEfficiencyKmpl + effJitter) * 10).toInt() / 10f
                )
            }
        }
    }

    fun triggerScan(onComplete: (Boolean) -> Unit) {
        _telemetry.value = _telemetry.value.copy(isScanning = true)
        scope.launch(Dispatchers.Default) {
            delay(2500L)
            _telemetry.value = _telemetry.value.copy(
                isScanning = false,
                isCheckEngineLit = false,
                engineHealthScore = 98
            )
            onComplete(true)
        }
    }

    fun clearCodes() {
        _telemetry.value = _telemetry.value.copy(
            isCheckEngineLit = false,
            engineHealthScore = 100
        )
    }

    fun stop() {
        telemetryJob?.cancel()
    }
}
