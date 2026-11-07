package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.DtcDao
import com.example.data.dao.MaintenanceDao
import com.example.data.dao.TripDao
import com.example.data.entity.DtcCodeEntity
import com.example.data.entity.MaintenanceAlertEntity
import com.example.data.entity.TripLogEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        TripLogEntity::class,
        DtcCodeEntity::class,
        MaintenanceAlertEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class CarHeadUnitDatabase : RoomDatabase() {

    abstract fun tripDao(): TripDao
    abstract fun dtcDao(): DtcDao
    abstract fun maintenanceDao(): MaintenanceDao

    companion object {
        @Volatile
        private var INSTANCE: CarHeadUnitDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): CarHeadUnitDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CarHeadUnitDatabase::class.java,
                    "celerio_car_database.db"
                )
                    .addCallback(DatabaseCallback(scope))
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialData(database)
                    }
                }
            }

            private suspend fun populateInitialData(database: CarHeadUnitDatabase) {
                val now = System.currentTimeMillis()
                // Initial Trip Logs for Maruti Suzuki Celerio
                val tripDao = database.tripDao()
                tripDao.insertTrip(
                    TripLogEntity(
                        title = "Highway Cruise - NH48",
                        distanceKm = 142.5f,
                        durationMinutes = 118,
                        avgSpeedKmh = 72.4f,
                        topSpeedKmh = 104.0f,
                        fuelConsumedLiters = 5.9f,
                        fuelEfficiencyKmpl = 24.1f, // Celerio K10C DualJet high efficiency
                        estimatedCostInr = 566.4f,
                        startLocationName = "Sector 29, Gurgaon",
                        destinationName = "Neemrana Fort",
                        timestamp = now - 86400000L
                    )
                )
                tripDao.insertTrip(
                    TripLogEntity(
                        title = "City Commute & Ring Road",
                        distanceKm = 28.4f,
                        durationMinutes = 48,
                        avgSpeedKmh = 35.5f,
                        topSpeedKmh = 68.0f,
                        fuelConsumedLiters = 1.35f,
                        fuelEfficiencyKmpl = 21.0f,
                        estimatedCostInr = 129.6f,
                        startLocationName = "Home Garage",
                        destinationName = "Cyber Hub",
                        timestamp = now - (86400000L * 2)
                    )
                )

                // Celerio Maintenance Alerts
                val maintenanceDao = database.maintenanceDao()
                maintenanceDao.insertAlerts(
                    listOf(
                        MaintenanceAlertEntity(
                            title = "Engine Oil & Filter (0W-20)",
                            category = "Lubrication",
                            dueKm = 10000,
                            currentKm = 8450,
                            status = "HEALTHY",
                            notes = "Maruti Genuine Ecstar 0W-20 Full Synthetic recommended",
                            lastServiceDate = "15 Jun 2026"
                        ),
                        MaintenanceAlertEntity(
                            title = "Tire Rotation & Pressure Check",
                            category = "Tires",
                            dueKm = 9000,
                            currentKm = 8450,
                            status = "DUE_SOON",
                            notes = "Front-to-back cross rotation. Check 33 PSI cold pressure.",
                            lastServiceDate = "12 Feb 2026"
                        ),
                        MaintenanceAlertEntity(
                            title = "Cabin & Air Intake Filter",
                            category = "Filters",
                            dueKm = 15000,
                            currentKm = 8450,
                            status = "HEALTHY",
                            notes = "Inspect pleated paper filter for dust & pollen blockage",
                            lastServiceDate = "15 Jun 2026"
                        ),
                        MaintenanceAlertEntity(
                            title = "Brake Pads & Fluid Inspection",
                            category = "Brakes",
                            dueKm = 20000,
                            currentKm = 8450,
                            status = "HEALTHY",
                            notes = "Front disc wear check; DOT 4 reservoir level check",
                            lastServiceDate = "15 Jun 2026"
                        )
                    )
                )

                // Sample OBD-II DTC diagnostic codes for demonstration
                val dtcDao = database.dtcDao()
                dtcDao.insertDtcCode(
                    DtcCodeEntity(
                        code = "P0135",
                        system = "O2 Sensor Heater Circuit",
                        description = "Oxygen Sensor Heater Circuit Malfunction (Bank 1, Sensor 1)",
                        severity = "WARNING",
                        possibleFix = "Inspect pre-cat lambda sensor harness wiring or heater resistance."
                    )
                )
            }
        }
    }
}
