package com.masterz.antispy.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.masterz.antispy.data.AppDatabase
import com.masterz.antispy.data.SensorUsageEvent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HistoryViewModel(app: Application) : AndroidViewModel(app) {
    private val dao = AppDatabase.getInstance(app).sensorUsageEventDao()
    private val _events = MutableStateFlow<List<SensorUsageEvent>>(emptyList())
    val events: StateFlow<List<SensorUsageEvent>> = _events

    init {
        loadEvents()
    }

    fun loadEvents() {
        viewModelScope.launch {
            _events.value = dao.getAllEvents()
        }
    }
}
