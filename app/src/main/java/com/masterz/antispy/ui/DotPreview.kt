package com.masterz.antispy.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun DotPreview(color: Color = Color.Green) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .background(color, shape = androidx.compose.foundation.shape.CircleShape)
    )
}
