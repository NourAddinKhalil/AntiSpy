package com.masterz.antispy.data

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SensorUsageRepository(context: Context) {
    private val db = AppDatabase.getInstance(context)
    private val dao = db.sensorUsageEventDao()

    fun logEvent(packageName: String, sensorType: String, appName: String = "", timestamp: Long = System.currentTimeMillis()) {
        CoroutineScope(Dispatchers.IO).launch {
            dao.insert(SensorUsageEvent(
                packageName = packageName,
                sensorType = sensorType,
                timestamp = timestamp,
                appName = appName
            ))
        }
    }

    suspend fun getAllEvents(): List<SensorUsageEvent> = dao.getAllEvents()

    fun clearAll() {
        CoroutineScope(Dispatchers.IO).launch {
            dao.clearAll()
        }
    }
}
