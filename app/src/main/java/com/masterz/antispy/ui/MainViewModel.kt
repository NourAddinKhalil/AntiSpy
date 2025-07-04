package com.masterz.antispy.ui

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val _isAccessibilityServiceEnabled = MutableLiveData<Boolean>()
    val isAccessibilityServiceEnabled: LiveData<Boolean> = _isAccessibilityServiceEnabled

    fun checkAccessibilityServiceEnabled() {
        viewModelScope.launch(Dispatchers.IO) {
            val context = getApplication<Application>().applicationContext
            val enabled = isAccessibilityServiceEnabled(context)
            _isAccessibilityServiceEnabled.postValue(enabled)
        }
    }

    fun openAccessibilitySettings(context: Context) {
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    private fun isAccessibilityServiceEnabled(context: Context): Boolean {
        val expectedComponentName = ComponentName(context, "com.masterz.antispy.service.AccessibilityListenerService")
        val enabledServices = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false
        return enabledServices.split(":").any {
            ComponentName.unflattenFromString(it) == expectedComponentName
        }
    }

    fun refreshAccessibilityStatus() {
        checkAccessibilityServiceEnabled()
    }

    fun disableTracking(context: Context) {
        // Stop the accessibility service and remove sticky notification
        try {
            val stopIntent = Intent(context, Class.forName("com.masterz.antispy.service.AccessibilityListenerService"))
            context.stopService(stopIntent)
        } catch (e: Exception) {
            // Optionally log error
        }
        // Remove sticky notification if present
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
        nm.cancel(2) // 2 is NOTIF_ID in AccessibilityListenerService
        // Optionally update UI state
        _isAccessibilityServiceEnabled.postValue(false)
    }

    fun enableTracking(context: Context) {
        // Only start service if accessibility is already enabled
        if (isAccessibilityServiceEnabled(context)) {
            try {
                val startIntent = Intent(context, Class.forName("com.masterz.antispy.service.AccessibilityListenerService"))
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(startIntent)
                } else {
                    context.startService(startIntent)
                }
            } catch (e: Exception) {
                // Optionally log error
            }
        }
    }
}
