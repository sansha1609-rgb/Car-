package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "dtc_codes")
data class DtcCodeEntity(
    @PrimaryKey
    val code: String, // e.g. "P0100", "P0420", "P0301"
    val system: String, // e.g. "Air / Fuel Metering", "Emissions", "Ignition"
    val description: String,
    val severity: String, // "CRITICAL", "WARNING", "INFO"
    val possibleFix: String,
    val detectedTimestamp: Long = System.currentTimeMillis(),
    val isCleared: Boolean = false
)
