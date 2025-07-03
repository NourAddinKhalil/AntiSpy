package com.masterz.antispy.ui

import android.content.Context
import android.provider.Settings
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.masterz.antispy.util.PermissionUtils

@Composable
fun PermissionsCheck() {
    val context = LocalContext.current
    val overlay = PermissionUtils.hasOverlayPermission(context)
    val usage = PermissionUtils.hasUsageStatsPermission(context)
    val accessibility = PermissionUtils.hasAccessibilityPermission(context)
    Column {
        Text("Overlay: ${if (overlay) "Granted" else "Missing"}")
        Text("Usage Stats: ${if (usage) "Granted" else "Missing"}")
        Text("Accessibility: ${if (accessibility) "Granted" else "Missing"}")
    }
}
