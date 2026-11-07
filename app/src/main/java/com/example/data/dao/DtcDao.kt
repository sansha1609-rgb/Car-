package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.entity.DtcCodeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DtcDao {
    @Query("SELECT * FROM dtc_codes WHERE isCleared = 0 ORDER BY detectedTimestamp DESC")
    fun getActiveDtcCodes(): Flow<List<DtcCodeEntity>>

    @Query("SELECT * FROM dtc_codes ORDER BY detectedTimestamp DESC")
    fun getAllDtcCodes(): Flow<List<DtcCodeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDtcCode(code: DtcCodeEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDtcCodes(codes: List<DtcCodeEntity>)

    @Query("UPDATE dtc_codes SET isCleared = 1")
    suspend fun clearAllDtcCodes()

    @Query("DELETE FROM dtc_codes")
    suspend fun deleteAll()
}
