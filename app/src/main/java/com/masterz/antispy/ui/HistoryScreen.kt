@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.masterz.antispy.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.core.graphics.drawable.toBitmap
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.dp
import com.masterz.antispy.data.SensorUsageEvent
import com.masterz.antispy.util.AppUtils

@Composable
fun HistoryScreen(viewModel: HistoryViewModel, onBack: () -> Unit) {
    val events by viewModel.events.collectAsState()
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sensor Usage History") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (events.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = androidx.compose.ui.Alignment.Center) {
                Text("No sensor usage events recorded.")
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(padding)) {
                items(events) { event ->
                    HistoryItem(event)
                }
            }
        }
    }
}

@Composable
fun HistoryItem(event: SensorUsageEvent) {
    val context = LocalContext.current
    val appIcon = remember(event.packageName) { AppUtils.getAppIcon(context, event.packageName) }
    Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
        if (appIcon != null) {
            Image(
                bitmap = appIcon.toBitmap().asImageBitmap(),
                contentDescription = null,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
        }
        Column {
            Text(text = "${event.appName} (${event.packageName})", style = MaterialTheme.typography.bodyLarge)
            Text(text = "Sensor: ${event.sensorType}", style = MaterialTheme.typography.bodyMedium)
            Text(text = "Time: ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(java.util.Date(event.timestamp))}", style = MaterialTheme.typography.bodySmall)
        }
    }
    Divider()
}
