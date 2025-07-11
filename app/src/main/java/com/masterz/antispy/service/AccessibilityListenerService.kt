package com.masterz.antispy.service

import android.Manifest
import android.accessibilityservice.AccessibilityService
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CameraManager.AvailabilityCallback
import android.location.GnssStatus
import android.location.LocationManager
import android.media.AudioManager
import android.media.AudioManager.AudioRecordingCallback
import android.media.AudioRecordingConfiguration
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.widget.ImageView
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.graphics.drawable.toBitmap
import com.masterz.antispy.R
import com.masterz.antispy.ui.MainActivity
import com.masterz.antispy.model.SensorType
import com.masterz.antispy.data.SensorUsageRepository
import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.provider.Settings.ACTION_USAGE_ACCESS_SETTINGS
import com.masterz.antispy.util.AppUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AccessibilityListenerService : AccessibilityService() {
    private var windowManager: WindowManager? = null
    private var overlayView: View? = null
    private val CHANNEL_ID = "antispy_channel"
    private val NOTIF_ID = 2

    private lateinit var cameraManager: CameraManager
    private lateinit var cameraCallback: AvailabilityCallback
    private lateinit var audioManager: AudioManager
    private lateinit var micCallback: AudioRecordingCallback
    private lateinit var locationManager: LocationManager
    private lateinit var sensorUsageRepository: SensorUsageRepository
    private lateinit var locationListener: android.location.LocationListener
    private var isCameraInUse = false
    private var isMicInUse = false
    private var isLocInUse = false

    private fun showSensorNotification(sensorType: String, packageName: String) {
        if (!com.masterz.antispy.util.Preferences.isTrackingEnabled(this)) return
        val appName = AppUtils.getAppName(this, packageName)
        val appIconDrawable = AppUtils.getAppIcon(this, packageName)
        val appIconBitmap: Bitmap? = appIconDrawable?.toBitmap(64, 64)
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notifId = when (sensorType) {
            "camera" -> 100
            "microphone" -> 101
            "location" -> 102
            else -> 103
        }
        val notif = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Sensor Access Detected")
            .setContentText("$appName ($packageName) used $sensorType")
            .setSmallIcon(R.drawable.ic_camera)
            .setLargeIcon(appIconBitmap)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()
        nm.notify(notifId, notif)
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        if (!com.masterz.antispy.util.Preferences.isTrackingEnabled(this)) {
            stopSelf()
            return
        }
        sensorUsageRepository = SensorUsageRepository(this)
        setupOverlay()
        createNotificationChannel()
        startForegroundServiceCompat()
        // Register hardware callbacks
        cameraManager = getSystemService(CAMERA_SERVICE) as CameraManager
        cameraCallback = object : AvailabilityCallback() {
            override fun onCameraAvailable(cameraId: String) {
                isCameraInUse = false
                updateDotOverlay()
            }
            override fun onCameraUnavailable(cameraId: String) {
                // Check camera preference
                if (com.masterz.antispy.util.Preferences.isCameraEnabled(this@AccessibilityListenerService)) {
                    isCameraInUse = true
                    logSensorUsage("camera")
                    showSensorNotification("camera", getForegroundAppPackageName() ?: "unknown")
                } else {
                    isCameraInUse = false
                }
                updateDotOverlay()
            }
        }
        cameraManager.registerAvailabilityCallback(cameraCallback, null)
        audioManager = getSystemService(AUDIO_SERVICE) as AudioManager
        micCallback = object : AudioRecordingCallback() {
            override fun onRecordingConfigChanged(configs: List<AudioRecordingConfiguration>) {
                // Check mic preference
                if (com.masterz.antispy.util.Preferences.isMicEnabled(this@AccessibilityListenerService)) {
                    isMicInUse = configs.isNotEmpty()
                    if (isMicInUse) {
                        logSensorUsage("microphone")
                        showSensorNotification("microphone", getForegroundAppPackageName() ?: "unknown")
                    }
                } else {
                    isMicInUse = false
                }
                updateDotOverlay()
            }
        }
        audioManager.registerAudioRecordingCallback(micCallback, null)
        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        locationListener = android.location.LocationListener { }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // Only register GnssStatus.Callback for passive detection
            locationManager.registerGnssStatusCallback(object : GnssStatus.Callback() {
                override fun onStarted() {
                    // Check GPS preference
                    if (com.masterz.antispy.util.Preferences.isGpsEnabled(this@AccessibilityListenerService)) {
                        isLocInUse = true
                        logSensorUsage("location")
                        showSensorNotification("location", getForegroundAppPackageName() ?: "unknown")
                    } else {
                        isLocInUse = false
                    }
                    updateDotOverlay()
                }
                override fun onStopped() {
                    isLocInUse = false
                    updateDotOverlay()
                }
            }, null)
        } else {
            // Request location permission if not granted
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                intent.data = android.net.Uri.parse("package:" + packageName)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
            // Optionally, show a notification or log that location permission is required
        }
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                startForeground(NOTIF_ID, notification)
            }
        } else {
            startForeground(NOTIF_ID, notification)
        }
    }

    private fun updateDotOverlay() {
        if (!com.masterz.antispy.util.Preferences.isTrackingEnabled(this)) {
            overlayView?.visibility = View.GONE
            return
        }
        // Hide all by default
        overlayView?.findViewById<View>(R.id.dot_camera_container)?.visibility = View.GONE
        overlayView?.findViewById<View>(R.id.dot_mic_container)?.visibility = View.GONE
        overlayView?.findViewById<View>(R.id.dot_gps_container)?.visibility = View.GONE
        // Show the active dot only if enabled in preferences
        val cameraEnabled = com.masterz.antispy.util.Preferences.isCameraEnabled(this)
        val micEnabled = com.masterz.antispy.util.Preferences.isMicEnabled(this)
        val gpsEnabled = com.masterz.antispy.util.Preferences.isGpsEnabled(this)
        when {
            isCameraInUse && cameraEnabled -> overlayView?.findViewById<View>(R.id.dot_camera_container)?.visibility = View.VISIBLE
            isMicInUse && micEnabled -> overlayView?.findViewById<View>(R.id.dot_mic_container)?.visibility = View.VISIBLE
            isLocInUse && gpsEnabled -> overlayView?.findViewById<View>(R.id.dot_gps_container)?.visibility = View.VISIBLE
            else -> overlayView?.visibility = View.GONE
        }
        // Always show overlay if any enabled sensor is in use, else hide
        overlayView?.visibility = if ((isCameraInUse && cameraEnabled) || (isMicInUse && micEnabled) || (isLocInUse && gpsEnabled)) View.VISIBLE else View.GONE
    }

    private fun logSensorUsage(sensorType: String) {
        if (!com.masterz.antispy.util.Preferences.isTrackingEnabled(this)) return
        val packageName = getForegroundAppPackageName() ?: "unknown"
        val appName = AppUtils.getAppName(this, packageName)
        sensorUsageRepository.logEvent(packageName, sensorType, appName)
    }

    private fun getForegroundAppPackageName(): String? {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val usm = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
            val time = System.currentTimeMillis()
            val appList = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, time - 1000 * 10, time)
            if (appList != null && appList.isNotEmpty()) {
                val recentApp = appList.maxByOrNull { it.lastTimeUsed }
                if (recentApp != null && recentApp.packageName != packageName) {
                    return recentApp.packageName
                }
            }
            // If usage access not granted, prompt user
            if (!hasUsageStatsPermission()) {
                val intent = Intent(ACTION_USAGE_ACCESS_SETTINGS)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
        }
        return packageName
    }

    private fun hasUsageStatsPermission(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val usm = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
            val time = System.currentTimeMillis()
            val stats = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, time - 1000 * 10, time)
            return stats != null && stats.isNotEmpty()
        }
        return false
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (!com.masterz.antispy.util.Preferences.isTrackingEnabled(this)) return
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
        if (::cameraManager.isInitialized && ::cameraCallback.isInitialized) {
            cameraManager.unregisterAvailabilityCallback(cameraCallback)
        }
        if (::audioManager.isInitialized && ::micCallback.isInitialized) {
            audioManager.unregisterAudioRecordingCallback(micCallback)
        }
        if (::locationManager.isInitialized && ::locationListener.isInitialized) {
            locationManager.removeUpdates(locationListener)
        }
        if (overlayView != null) windowManager?.removeView(overlayView)
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.cancel(NOTIF_ID)
    }

    override fun onUnbind(intent: Intent?): Boolean {
        // Remove sticky notification if tracking is disabled
        if (!com.masterz.antispy.util.Preferences.isTrackingEnabled(this)) {
            val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.cancel(NOTIF_ID)
        }
        return super.onUnbind(intent)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == "com.masterz.antispy.STOP_FOREGROUND") {
            stopForeground(true)
            val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.cancel(NOTIF_ID)
            stopSelf()
            return START_NOT_STICKY
        }
        // If service is started without accessibility permission or tracking disabled, stop immediately
        if (!com.masterz.antispy.util.Preferences.isTrackingEnabled(this) || !isServiceEnabled()) {
            stopSelf()
            return START_NOT_STICKY
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun isServiceEnabled(): Boolean {
        val expectedComponentName = ComponentName(this, AccessibilityListenerService::class.java)
        val enabledServices = Settings.Secure.getString(
            contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false
        return enabledServices.split(":").any {
            ComponentName.unflattenFromString(it) == expectedComponentName
        }
    }
}
