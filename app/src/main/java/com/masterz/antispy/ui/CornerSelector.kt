package com.masterz.antispy.ui

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*

@Composable
fun CornerSelector(selected: Int, onSelect: (Int) -> Unit) {
    val corners = listOf("Top-Left", "Top-Right", "Bottom-Left", "Bottom-Right")
    Row {
        corners.forEachIndexed { idx, label ->
            RadioButton(selected = selected == idx, onClick = { onSelect(idx) })
            Text(label)
        }
    }
}
