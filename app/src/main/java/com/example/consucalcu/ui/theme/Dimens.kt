package com.example.consucalcu.ui.theme

import androidx.compose.ui.unit.dp

// Corner-radius scale — single source of truth so shapes stay consistent across screens.
object ConsuRadius {
    val hairline = 2.dp   // progress-bar caps / thin decorative bars
    val track = 3.dp      // progress-bar tracks
    val xs = 4.dp
    val sm = 8.dp
    val dropdown = 10.dp  // dropdown/exposed-menu fields
    val md = 12.dp
    val badge = 14.dp     // icon badges/pills
    val lg = 16.dp
    val xl = 20.dp
    val xxl = 24.dp
}

// Spacing scale — matches the 4dp grid already used across padding/Arrangement.spacedBy calls.
object ConsuSpacing {
    val xs = 4.dp
    val sm = 8.dp
    val md = 12.dp
    val lg = 16.dp
    val xl = 20.dp
    val xxl = 24.dp
    val xxxl = 32.dp
}
