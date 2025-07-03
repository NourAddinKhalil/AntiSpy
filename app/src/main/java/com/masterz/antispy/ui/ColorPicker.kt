package com.masterz.antispy.ui

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.AlertDialog
import androidx.compose.ui.Modifier
import androidx.compose.foundation.background

@Composable
fun ColorPicker(currentColor: Color, onColorChange: (Color) -> Unit) {
    var showDialog by remember { mutableStateOf(false) }
    Row {
        Button(onClick = { showDialog = true }) {
            Text("Pick Dot Color")
        }
    }
    if (showDialog) {
        // Simple color picker dialog (placeholder)
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Select Dot Color") },
            text = {
                Row {
                    listOf(Color.Green, Color.Red, Color.Blue, Color.Yellow).forEach { color ->
                        Button(onClick = {
                            onColorChange(color)
                            showDialog = false
                        }, modifier = Modifier.background(color)) {
                        }
                    }
                }
            },
            confirmButton = {}
        )
    }
}
