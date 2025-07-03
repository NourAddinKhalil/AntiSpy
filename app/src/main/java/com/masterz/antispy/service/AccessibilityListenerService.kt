package com.masterz.antispy.service

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
import com.masterz.antispy.model.SensorType

class AccessibilityListenerService : AccessibilityService() {
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // Example: Detect camera/mic/gps keywords in window titles (very basic)
        val packageName = event?.packageName?.toString() ?: return
        val eventText = event.text?.joinToString(" ") ?: ""
        var detected: SensorType? = null
        if (eventText.contains("camera", true)) detected = SensorType.CAMERA
        else if (eventText.contains("mic", true)) detected = SensorType.MICROPHONE
        else if (eventText.contains("gps", true) || eventText.contains("location", true)) detected = SensorType.GPS
        if (detected != null) {
            // Notify OverlayService
            val intent = Intent("com.masterz.antispy.SENSOR_STATUS")
            intent.putExtra("sensor", detected.name)
            intent.putExtra("package", packageName)
            sendBroadcast(intent)
        }
    }

    override fun onInterrupt() {}
}
