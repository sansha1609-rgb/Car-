package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.entity.TripLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TripDao {
    @Query("SELECT * FROM trip_logs ORDER BY timestamp DESC")
    fun getAllTrips(): Flow<List<TripLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrip(trip: TripLogEntity): Long

    @Query("DELETE FROM trip_logs WHERE id = :tripId")
    suspend fun deleteTrip(tripId: Long)

    @Query("DELETE FROM trip_logs")
    suspend fun clearAllTrips()

    @Query("SELECT SUM(distanceKm) FROM trip_logs")
    fun getTotalDistance(): Flow<Float?>

    @Query("SELECT AVG(fuelEfficiencyKmpl) FROM trip_logs")
    fun getAverageFuelEfficiency(): Flow<Float?>
}
