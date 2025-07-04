package com.masterz.antispy.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.ArrowBack
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.masterz.antispy.util.Preferences
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()
    private var locationPermissionRequested = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= 33 && ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1001)
        }
        viewModel.checkAccessibilityServiceEnabled()
        setContent {
            val context = LocalContext.current
            var showLocationPermissionDialog by remember { mutableStateOf(false) }
            val locationPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
                if (!isGranted) {
                    showLocationPermissionDialog = true
                }
            }
            val lifecycleOwner = LocalLifecycleOwner.current
            LaunchedEffect(lifecycleOwner) {
                val observer = LifecycleEventObserver { _, event ->
                    if (event == Lifecycle.Event.ON_RESUME) {
                        viewModel.refreshAccessibilityStatus()
                    }
                }
                lifecycleOwner.lifecycle.addObserver(observer)
            }
            MaterialTheme {
                val navController = rememberNavController()
                NavHost(navController, startDestination = "main") {
                    composable("main") {
                        MainScreen(
                            viewModel,
                            onShowHistory = { navController.navigate("history") },
                            onCustomizeDots = { navController.navigate("customize") },
                            onRequestLocationPermission = {
                                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                    locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                                }
                            },
                            showLocationPermissionDialog = showLocationPermissionDialog,
                            onDismissLocationDialog = { showLocationPermissionDialog = false }
                        )
                    }
                    composable("history") {
                        val context = LocalContext.current.applicationContext
                        val historyViewModel = remember {
                            HistoryViewModel(context as android.app.Application)
                        }
                        HistoryScreen(historyViewModel) { navController.popBackStack() }
                    }
                    composable("customize") {
                        CustomizeDotsScreen(onBack = { navController.popBackStack() })
                    }
                }
            }
        }
    }
}

@Composable
fun MainScreen(
    viewModel: MainViewModel,
    onShowHistory: () -> Unit,
    onCustomizeDots: () -> Unit,
    onRequestLocationPermission: () -> Unit = {},
    showLocationPermissionDialog: Boolean = false,
    onDismissLocationDialog: () -> Unit = {}
) {
    val isAccessibilityEnabled by viewModel.isAccessibilityServiceEnabled.observeAsState(false)
    val context = LocalContext.current
    var cameraEnabled by remember { mutableStateOf(Preferences.isCameraEnabled(context)) }
    var micEnabled by remember { mutableStateOf(Preferences.isMicEnabled(context)) }
    var gpsEnabled by remember { mutableStateOf(Preferences.isGpsEnabled(context)) }
    val scrollState = rememberScrollState()
    var showPermissionDialog by remember { mutableStateOf(false) }
    val canDrawOverlays = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) Settings.canDrawOverlays(context) else true

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(8.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Grant overlay permission if needed
        if (!canDrawOverlays) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "To enable AntiSpy features, please grant overlay permission.",
                        fontSize = 18.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(onClick = {
                        val intent = android.content.Intent(android.provider.Settings.ACTION_MANAGE_OVERLAY_PERMISSION, android.net.Uri.parse("package:" + context.packageName))
                        context.startActivity(intent)
                    }) {
                        Text("Grant Permission")
                    }
                }
            }
        }
        // Enable tracking (accessibility)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Enable Tracking", fontSize = 18.sp, modifier = Modifier.weight(1f))
            Switch(
                checked = isAccessibilityEnabled,
                onCheckedChange = { enabled ->
                    if (!isAccessibilityEnabled && enabled) {
                        viewModel.openAccessibilitySettings(context)
                        showPermissionDialog = true
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
        // Accessibility Status Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    if (!isAccessibilityEnabled) {
                        viewModel.openAccessibilitySettings(context)
                    }
                },
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
                    onCheckedChange = {
                        cameraEnabled = it
                        Preferences.setCameraEnabled(context, it)
                    },
                    color = Color(0xFF7C4DFF)
                )
                Divider()
                SettingSwitch(
                    icon = Icons.Default.Mic,
                    title = "Microphone",
                    checked = micEnabled,
                    onCheckedChange = {
                        micEnabled = it
                        Preferences.setMicEnabled(context, it)
                    },
                    color = Color(0xFFFF9800)
                )
                Divider()
                SettingSwitch(
                    icon = Icons.Default.LocationOn,
                    title = "Location",
                    checked = gpsEnabled,
                    onCheckedChange = {
                        gpsEnabled = it
                        Preferences.setGpsEnabled(context, it)
                        if (it) {
                            onRequestLocationPermission()
                        }
                    },
                    color = Color(0xFF2196F3)
                )
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
    if (showLocationPermissionDialog) {
        AlertDialog(
            onDismissRequest = onDismissLocationDialog,
            title = { Text("Location Permission Required") },
            text = { Text("To detect GPS usage, AntiSpy needs location permission. This is only used for passive detection and not for accessing your location.") },
            confirmButton = {
                Button(onClick = {
                    onDismissLocationDialog()
                }) { Text("OK") }
            }
        )
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

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun CustomizeDotsScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Customize Dots") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            SettingsScreen()
        }
    }
}
