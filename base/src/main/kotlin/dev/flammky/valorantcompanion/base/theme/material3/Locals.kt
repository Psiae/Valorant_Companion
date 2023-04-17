package dev.flammky.valorantcompanion.base.theme.material3

import androidx.compose.runtime.staticCompositionLocalOf

val LocalIsThemeDark = staticCompositionLocalOf<Boolean> { false }
val LocalDarkColorScheme = staticCompositionLocalOf { defaultDarkColorScheme() }
val LocalLightColorScheme = staticCompositionLocalOf { defaultLightColorScheme() }
val LocalColorScheme = staticCompositionLocalOf { defaultLightColorScheme() }