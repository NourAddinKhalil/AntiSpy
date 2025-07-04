package com.masterz.antispy.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.masterz.antispy.R
import com.masterz.antispy.model.SensorType
import com.masterz.antispy.ui.MainActivity

class OverlayService : Service() {
    private var windowManager: WindowManager? = null
    private var overlayView: View? = null

    private val sensorReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val sensor = intent?.getStringExtra("sensor") ?: return
            val type = SensorType.valueOf(sensor)
            updateDotIcon(type)
        }
    }

    private val CHANNEL_ID = "antispy_channel"
    private val NOTIF_ID = 1
    private var currentSensor: SensorType? = null

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        addOverlay()
        // Only register receiver and post notifications if permission is granted (Android 13+)
        val hasNotifPerm = if (Build.VERSION.SDK_INT >= 33) {
            checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        } else true
        if (hasNotifPerm) {
            if (Build.VERSION.SDK_INT >= 34) {
                ContextCompat.registerReceiver(
                    this,
                    sensorReceiver,
                    IntentFilter("com.masterz.antispy.SENSOR_STATUS"),
                    ContextCompat.RECEIVER_NOT_EXPORTED
                )
            } else {
                registerReceiver(sensorReceiver, IntentFilter("com.masterz.antispy.SENSOR_STATUS"))
            }
            createNotificationChannel()
            startForeground(NOTIF_ID, buildNotification(null))
        }
    }

    private fun addOverlay() {
        if (overlayView != null) return
        overlayView = LayoutInflater.from(this).inflate(R.layout.overlay_dot, null)
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        )
        params.gravity = Gravity.TOP or Gravity.END // Default position
        windowManager?.addView(overlayView, params)
    }

    private fun updateDotIcon(type: SensorType) {
        currentSensor = type
        overlayView?.findViewById<ImageView>(R.id.icon_camera)?.visibility = if (type == SensorType.CAMERA) View.VISIBLE else View.GONE
        overlayView?.findViewById<ImageView>(R.id.icon_mic)?.visibility = if (type == SensorType.MICROPHONE) View.VISIBLE else View.GONE
        overlayView?.findViewById<ImageView>(R.id.icon_gps)?.visibility = if (type == SensorType.GPS) View.VISIBLE else View.GONE
        val notif = buildNotification(type)
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(NOTIF_ID, notif)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, "AntiSpy Sensor", NotificationManager.IMPORTANCE_LOW)
            val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(type: SensorType?): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pi = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        val text = when (type) {
            SensorType.CAMERA -> "Camera in use"
            SensorType.MICROPHONE -> "Microphone in use"
            SensorType.GPS -> "GPS in use"
            else -> "Monitoring sensors"
        }
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("AntiSpy")
            .setContentText(text)
            .setSmallIcon(R.drawable.ic_camera)
            .setContentIntent(pi)
            .setOngoing(true)
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (overlayView != null) windowManager?.removeView(overlayView)
        unregisterReceiver(sensorReceiver)
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
