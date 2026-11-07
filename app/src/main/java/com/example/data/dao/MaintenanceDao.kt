package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.entity.MaintenanceAlertEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MaintenanceDao {
    @Query("SELECT * FROM maintenance_alerts ORDER BY dueKm ASC")
    fun getAllAlerts(): Flow<List<MaintenanceAlertEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlerts(alerts: List<MaintenanceAlertEntity>)

    @Query("UPDATE maintenance_alerts SET status = 'HEALTHY', lastServiceDate = :date WHERE id = :id")
    suspend fun markServiced(id: Long, date: String)

    @Query("DELETE FROM maintenance_alerts")
    suspend fun deleteAll()
}
