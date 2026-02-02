package com.sparsa.android.main.helpers.colors

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color


@Composable
fun mainColor(): Color {
    return Color(if(isSystemInDarkTheme()) 0xFF105ba1 else 0xFF0070C0)
}

@Composable
fun textColor(): Color {
    return Color(if(isSystemInDarkTheme()) 0xFF000000 else 0xFFFFFFFF)
}