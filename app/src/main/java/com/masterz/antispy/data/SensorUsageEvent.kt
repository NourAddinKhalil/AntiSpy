package com.masterz.antispy.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sensor_usage_events")
data class SensorUsageEvent(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val packageName: String,
    val sensorType: String, // e.g., "camera", "microphone", "location"
    val timestamp: Long // epoch millis
)
