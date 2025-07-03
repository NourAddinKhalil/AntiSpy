package com.masterz.antispy.model

enum class SensorType {
    CAMERA, MICROPHONE, GPS
}

// Represents the current status of each sensor
sealed class SensorStatus {
    object Idle : SensorStatus()
    data class Active(val type: SensorType, val packageName: String) : SensorStatus()
}
