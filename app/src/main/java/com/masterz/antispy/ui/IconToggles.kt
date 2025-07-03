package com.masterz.antispy.ui

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.runtime.*

@Composable
fun IconToggles(camera: Boolean, mic: Boolean, gps: Boolean, onChange: (Boolean, Boolean, Boolean) -> Unit) {
    Row {
        Checkbox(checked = camera, onCheckedChange = { onChange(it, mic, gps) })
        Text("Camera")
        Checkbox(checked = mic, onCheckedChange = { onChange(camera, it, gps) })
        Text("Mic")
        Checkbox(checked = gps, onCheckedChange = { onChange(camera, mic, it) })
        Text("GPS")
    }
}
