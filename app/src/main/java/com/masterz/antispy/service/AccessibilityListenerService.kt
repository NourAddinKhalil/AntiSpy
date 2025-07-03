package com.masterz.antispy.service

import android.accessibilityservice.AccessibilityService
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.widget.ImageView
import androidx.core.app.NotificationCompat
import com.masterz.antispy.R
import com.masterz.antispy.ui.MainActivity
import com.masterz.antispy.model.SensorType

class AccessibilityListenerService : AccessibilityService() {
    private var windowManager: WindowManager? = null
    private var overlayView: View? = null
    private val CHANNEL_ID = "antispy_channel"
    private val NOTIF_ID = 2

    override fun onServiceConnected() {
        super.onServiceConnected()
        setupOverlay()
        createNotificationChannel()
        startForegroundServiceCompat()
    }

    private fun setupOverlay() {
        if (overlayView != null) return
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        overlayView = LayoutInflater.from(this).inflate(R.layout.overlay_dot, null)
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY else WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        )
        params.gravity = Gravity.TOP or Gravity.END
        windowManager?.addView(overlayView, params)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, "AntiSpy Sensor", NotificationManager.IMPORTANCE_LOW)
            val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.createNotificationChannel(channel)
        }
    }

    private fun startForegroundServiceCompat() {
        val intent = Intent(this, MainActivity::class.java)
        val pi = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("AntiSpy")
            .setContentText("Monitoring sensors")
            .setSmallIcon(R.drawable.ic_camera)
            .setContentIntent(pi)
            .setOngoing(true)
            .build()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Use reflection to avoid compile error on lower APIs
            try {
                val mic = Class.forName("android.app.ServiceInfo").getField("FOREGROUND_SERVICE_TYPE_MICROPHONE").getInt(null)
                val cam = Class.forName("android.app.ServiceInfo").getField("FOREGROUND_SERVICE_TYPE_CAMERA").getInt(null)
                val loc = Class.forName("android.app.ServiceInfo").getField("FOREGROUND_SERVICE_TYPE_LOCATION").getInt(null)
                val types = mic or cam or loc
                val startForeground = this::class.java.getMethod("startForeground", Int::class.java, Notification::class.java, Int::class.java)
                startForeground.invoke(this, NOTIF_ID, notification, types)
            } catch (e: Exception) {
                startForeground(NOTIF_ID, notification)
            }
        } else {
            startForeground(NOTIF_ID, notification)
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        val packageName = event.packageName?.toString() ?: return
        val eventText = event.text?.joinToString(" ") ?: ""
        var detected: SensorType? = null
        if (eventText.contains("camera", true)) detected = SensorType.CAMERA
        else if (eventText.contains("mic", true)) detected = SensorType.MICROPHONE
        else if (eventText.contains("gps", true) || eventText.contains("location", true)) detected = SensorType.GPS
        if (detected != null) {
            try {
                val intent = Intent("com.masterz.antispy.SENSOR_STATUS")
                intent.putExtra("sensor", detected.name)
                intent.putExtra("package", packageName)
                sendBroadcast(intent)
            } catch (e: Exception) {
                // Optionally log error
            }
        }
    }

    override fun onInterrupt() {}

    override fun onDestroy() {
        super.onDestroy()
        if (overlayView != null) windowManager?.removeView(overlayView)
    }
}
