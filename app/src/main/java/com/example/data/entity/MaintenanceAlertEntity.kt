package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "maintenance_alerts")
data class MaintenanceAlertEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val category: String, // e.g., "Engine Oil", "Brake Fluid", "Air Filter", "Tire Rotation"
    val dueKm: Int,
    val currentKm: Int,
    val status: String, // "DUE_SOON", "OVERDUE", "HEALTHY"
    val notes: String,
    val lastServiceDate: String
)
