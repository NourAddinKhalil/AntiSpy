package com.masterz.antispy.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface SensorUsageEventDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(event: SensorUsageEvent)

    @Query("SELECT * FROM sensor_usage_events ORDER BY timestamp DESC")
    suspend fun getAllEvents(): List<SensorUsageEvent>

    @Query("DELETE FROM sensor_usage_events")
    suspend fun clearAll()
}
