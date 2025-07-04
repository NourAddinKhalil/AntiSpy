package com.masterz.antispy.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.masterz.antispy.service.OverlayService
import androidx.core.app.ActivityCompat
import androidx.compose.foundation.layout.width
import androidx.compose.ui.graphics.vector.ImageVector

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= 33 && ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1001)
        }
        viewModel.checkAccessibilityServiceEnabled()
        setContent {
            MaterialTheme {
                val navController = rememberNavController()
                NavHost(navController, startDestination = "main") {
                    composable("main") {
                        MainScreen(viewModel, onShowHistory = { navController.navigate("history") })
                    }
                    composable("history") {
                        val context = LocalContext.current.applicationContext
                        val historyViewModel = remember {
                            HistoryViewModel(context as android.app.Application)
                        }
                        HistoryScreen(historyViewModel) { navController.popBackStack() }
                    }
                }
            }
        }
    }
}

@Composable
fun MainScreen(viewModel: MainViewModel, onShowHistory: () -> Unit) {
    val isAccessibilityEnabled by viewModel.isAccessibilityServiceEnabled.observeAsState(false)
    var cameraEnabled by remember { mutableStateOf(true) }
    var micEnabled by remember { mutableStateOf(true) }
    var gpsEnabled by remember { mutableStateOf(true) }
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Accessibility Status Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (isAccessibilityEnabled) Icons.Default.CameraAlt else Icons.Default.CameraAlt,
                    contentDescription = null,
                    tint = if (isAccessibilityEnabled) Color(0xFF4CAF50) else Color(0xFFF44336),
                    modifier = Modifier.size(36.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = if (isAccessibilityEnabled) "Accessibility Service is ENABLED" else "Accessibility Service is DISABLED",
                        fontWeight = FontWeight.Bold,
                        color = if (isAccessibilityEnabled) Color(0xFF4CAF50) else Color(0xFFF44336),
                        fontSize = 18.sp
                    )
                    if (!isAccessibilityEnabled) {
                        Text(
                            text = "The dot will not work currently, tap to enable",
                            color = Color.Gray,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
        // Access Control Panel
        Text(
            text = "Access Control Panel",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(8.dp)) {
                SettingSwitch(
                    icon = Icons.Default.CameraAlt,
                    title = "Camera",
                    checked = cameraEnabled,
                    onCheckedChange = { cameraEnabled = it },
                    color = Color(0xFF7C4DFF)
                )
                Divider()
                SettingSwitch(
                    icon = Icons.Default.Mic,
                    title = "Microphone",
                    checked = micEnabled,
                    onCheckedChange = { micEnabled = it },
                    color = Color(0xFFFF9800)
                )
                Divider()
                SettingSwitch(
                    icon = Icons.Default.LocationOn,
                    title = "Location",
                    checked = gpsEnabled,
                    onCheckedChange = { gpsEnabled = it },
                    color = Color(0xFF2196F3)
                )
            }
        }
        // Customization Panel
        Text(
            text = "Customization Panel",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(8.dp)) {
                Text("Customisation Center", fontWeight = FontWeight.SemiBold)
                Text("Make dots look exactly like how you want them to look. You can change color, shape and many more.", fontSize = 14.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = { /* TODO: Open customization */ }) {
                    Text("Customize Dots")
                }
            }
        }
        // History Button
        Button(
            onClick = onShowHistory,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text("View Sensor Usage History")
        }
    }
}

@Composable
fun SettingSwitch(
    icon: ImageVector,
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    color: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(32.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Text(title, fontWeight = FontWeight.Medium, fontSize = 16.sp, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
