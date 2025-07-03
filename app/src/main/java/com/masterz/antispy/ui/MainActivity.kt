package com.masterz.antispy.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.platform.LocalContext
import com.masterz.antispy.service.OverlayService

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.checkAccessibilityServiceEnabled()
        setContent {
            MaterialTheme {
                MainScreen(viewModel)
            }
        }
    }
}

@Composable
fun MainScreen(viewModel: MainViewModel) {
    val isAccessibilityEnabled by viewModel.isAccessibilityServiceEnabled.observeAsState(false)
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("AntiSpy", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(24.dp))
        if (isAccessibilityEnabled) {
            Text("Accessibility Service is ENABLED", color = Color(0xFF4CAF50))
        } else {
            Text("Accessibility Service is DISABLED", color = Color(0xFFF44336))
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { viewModel.openAccessibilitySettings(context) }) {
                Text("Enable Accessibility Service")
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
        SettingsScreen()
    }
}
