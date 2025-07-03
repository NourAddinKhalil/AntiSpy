package com.masterz.antispy.ui
import android.accessibilityservice.AccessibilityServiceInfo
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.view.accessibility.AccessibilityManager
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.masterz.antispy.util.Preferences

@Composable
fun SettingsScreen() {
    val context = LocalContext.current
    val activity = context as? Activity
    val accessibilityManager = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
    var trackingEnabled by remember { mutableStateOf(isAccessibilityServiceEnabled(context)) }
    var showPermissionDialog by remember { mutableStateOf(false) }
    var usageHistory by remember { mutableStateOf(listOf<String>()) } // Replace with real history
    var dotColor by remember { mutableStateOf(Color.Green) }
    var corner by remember { mutableStateOf(0) }
    var cameraEnabled by remember { mutableStateOf(true) }
    var micEnabled by remember { mutableStateOf(true) }
    var gpsEnabled by remember { mutableStateOf(true) }

    // Load preferences
    LaunchedEffect(Unit) {
        dotColor = Color(Preferences.getDotColor(context, 0xFF00FF00.toInt()))
        corner = Preferences.getDotCorner(context, 0)
        cameraEnabled = Preferences.isCameraEnabled(context)
        micEnabled = Preferences.isMicEnabled(context)
        gpsEnabled = Preferences.isGpsEnabled(context)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("AntiSpy", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Enable Tracking", fontSize = 18.sp, modifier = Modifier.weight(1f))
            Switch(
                checked = trackingEnabled,
                onCheckedChange = { enabled ->
                    if (enabled) {
                        // Request accessibility permission
                        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                        context.startActivity(intent)
                        showPermissionDialog = true
                    } else {
                        // Optionally: show info or stop service
                        trackingEnabled = false
                    }
                },
                colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.primary)
            )
        }
        if (showPermissionDialog) {
            Text(
                "Please enable AntiSpy Accessibility Service in the list to allow tracking.",
                color = MaterialTheme.colorScheme.error,
                fontSize = 16.sp
            )
        }
        if (trackingEnabled) {
            Text("Tracking is enabled. Usage history:", fontWeight = FontWeight.SemiBold)
            LazyColumn(modifier = Modifier.weight(1f, fill = false)) {
                items(usageHistory) { item ->
                    Text(item, fontSize = 15.sp)
                }
            }
        } else {
            Text("Tracking is disabled.", color = Color.Gray)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(context)) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "To enable AntiSpy features, please grant overlay permission.",
                            fontSize = 18.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(onClick = {
                            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + context.packageName))
                            context.startActivity(intent)
                        }) {
                            Text("Grant Permission")
                        }
                    }
                }
            }
        }

        DotPreview(color = dotColor)
        ColorPicker(dotColor) { color ->
            dotColor = color
            Preferences.setDotColor(context, color.value.toInt())
        }
        IconToggles(cameraEnabled, micEnabled, gpsEnabled, onChange = { cam, mic, gps ->
            cameraEnabled = cam
            micEnabled = mic
            gpsEnabled = gps
            Preferences.setCameraEnabled(context, cam)
            Preferences.setMicEnabled(context, mic)
            Preferences.setGpsEnabled(context, gps)
        })
        CornerSelector(corner) { idx ->
            corner = idx
            Preferences.setDotCorner(context, idx)
        }
    }
}

fun isAccessibilityServiceEnabled(context: Context): Boolean {
    val am = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
    val enabledServices = Settings.Secure.getString(
        context.contentResolver,
        Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
    ) ?: return false
    return enabledServices.contains(context.packageName + "/com.masterz.antispy.service.AccessibilityListenerService")
}
