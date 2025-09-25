package com.cs407.cardfolio.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)

val LightGradientTop = Color(0xFFE0E0E0)
val LightGradientBottom = Color(0xFFFFCDD2)
val DarkGradientTop = Color(0xFFBBDEFB)
val DarkGradientBottom = Color(0xFF424242)


data class CustomColors(
    val gradientTop: Color,
    val gradientBottom: Color
)
val LightCustomColors = CustomColors(
    gradientTop = LightGradientTop,
    gradientBottom = LightGradientBottom
)
val DarkCustomColors = CustomColors(
    gradientTop = DarkGradientTop,
    gradientBottom = DarkGradientBottom
)
val LocalCustomColors = staticCompositionLocalOf {
    CustomColors(
        gradientTop = Color.Unspecified,
        gradientBottom = Color.Unspecified
    )
}
