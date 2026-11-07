package com.example.data.repository

import com.example.data.dao.DtcDao
import com.example.data.dao.MaintenanceDao
import com.example.data.dao.TripDao
import com.example.data.entity.DtcCodeEntity
import com.example.data.entity.MaintenanceAlertEntity
import com.example.data.entity.TripLogEntity
import kotlinx.coroutines.flow.Flow

class CarRepository(
    private val tripDao: TripDao,
    private val dtcDao: DtcDao,
    private val maintenanceDao: MaintenanceDao
) {
    val allTrips: Flow<List<TripLogEntity>> = tripDao.getAllTrips()
    val activeDtcCodes: Flow<List<DtcCodeEntity>> = dtcDao.getActiveDtcCodes()
    val allDtcCodes: Flow<List<DtcCodeEntity>> = dtcDao.getAllDtcCodes()
    val maintenanceAlerts: Flow<List<MaintenanceAlertEntity>> = maintenanceDao.getAllAlerts()
    val totalDistance: Flow<Float?> = tripDao.getTotalDistance()
    val averageFuelEfficiency: Flow<Float?> = tripDao.getAverageFuelEfficiency()

    suspend fun saveTrip(trip: TripLogEntity): Long {
        return tripDao.insertTrip(trip)
    }

    suspend fun deleteTrip(tripId: Long) {
        tripDao.deleteTrip(tripId)
    }

    suspend fun clearTrips() {
        tripDao.clearAllTrips()
    }

    suspend fun addDtcCode(dtc: DtcCodeEntity) {
        dtcDao.insertDtcCode(dtc)
    }

    suspend fun clearAllDtcCodes() {
        dtcDao.clearAllDtcCodes()
    }

    suspend fun markMaintenanceServiced(alertId: Long, dateString: String) {
        maintenanceDao.markServiced(alertId, dateString)
    }
}
