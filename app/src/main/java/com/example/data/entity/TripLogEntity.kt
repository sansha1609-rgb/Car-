package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "trip_logs")
data class TripLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val distanceKm: Float,
    val durationMinutes: Int,
    val avgSpeedKmh: Float,
    val topSpeedKmh: Float,
    val fuelConsumedLiters: Float,
    val fuelEfficiencyKmpl: Float,
    val estimatedCostInr: Float,
    val startLocationName: String,
    val destinationName: String,
    val timestamp: Long = System.currentTimeMillis()
)
