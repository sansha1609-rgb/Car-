package com.example

import com.example.hardware.OnBoardComputerManager
import com.example.hardware.TripTimeFilter
import com.example.launcher.CarBrandLogo
import com.example.launcher.LauncherScreenTheme
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun fuel_efficiency_calculation_isCorrect() {
        val distanceKm = 100f
        val fuelLiters = 4.2f
        val efficiency = distanceKm / fuelLiters
        assertEquals(23.8f, (efficiency * 10).toInt() / 10f, 0.2f)
    }

    @Test
    fun onBoardComputer_durationFormatting_isCorrect() {
        val testScope = TestScope(UnconfinedTestDispatcher())
        val computer = OnBoardComputerManager(testScope)

        assertEquals("01:05", computer.formatDuration(65))
        assertEquals("01:00:00", computer.formatDuration(3600))
        assertEquals("02:30:15", computer.formatDuration(9015))
    }

    @Test
    fun onBoardComputer_resetTripData_clearsMetrics() {
        val testScope = TestScope(UnconfinedTestDispatcher())
        val computer = OnBoardComputerManager(testScope)

        computer.onSpeedUpdate(55f)
        assertTrue(computer.metrics.value.passableDistanceKm > 0f)

        computer.resetTripData()
        val metrics = computer.metrics.value
        assertEquals(0.0f, metrics.passableDistanceKm, 0.001f)
        assertEquals(0L, metrics.operatingTimeSeconds)
        assertEquals(0L, metrics.movingTimeSeconds)
        assertFalse(metrics.accel0To60.isCompleted)
    }

    @Test
    fun onBoardComputer_filterSwitching_works() {
        val testScope = TestScope(UnconfinedTestDispatcher())
        val computer = OnBoardComputerManager(testScope)

        computer.selectTimeFilter(TripTimeFilter.ALL_TIME)
        assertEquals(TripTimeFilter.ALL_TIME, computer.metrics.value.currentFilter)
        assertTrue(computer.metrics.value.passableDistanceKm > 1000f)

        computer.selectTimeFilter(TripTimeFilter.TRIP)
        assertEquals(TripTimeFilter.TRIP, computer.metrics.value.currentFilter)
    }

    @Test
    fun carLauncher_themesAndLogos_exist() {
        assertEquals(3, LauncherScreenTheme.values().size)
        assertTrue(CarBrandLogo.values().size >= 10)
    }
}

