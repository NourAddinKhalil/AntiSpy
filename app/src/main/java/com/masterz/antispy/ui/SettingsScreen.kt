package com.masterz.antispy.ui

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.masterz.antispy.util.Preferences

@Composable
fun SettingsScreen() {
    val context = LocalContext.current
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
        Text("AntiSpy Settings", style = MaterialTheme.typography.headlineSmall)
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
        PermissionsCheck()
    }
}
