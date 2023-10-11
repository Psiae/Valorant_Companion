package dev.flammky.valorantcompanion.base.theme.material3

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.State
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.graphics.Color

val Material3Theme.colorScheme: ColorScheme
    @Composable
    @ReadOnlyComposable
    get() = LocalColorScheme.current

val Material3Theme.lightColorScheme: ColorScheme
    @Composable
    @ReadOnlyComposable
    get() = LocalLightColorScheme.current

val Material3Theme.darkColorScheme: ColorScheme
    @Composable
    @ReadOnlyComposable
    get() = LocalDarkColorScheme.current

@Composable
fun Material3Theme.backgroundColorAsState(): State<Color> {
    return rememberUpdatedState(newValue = colorScheme.background)
}

@Composable
fun Material3Theme.backgroundContentColorAsState(): State<Color> {
    return rememberUpdatedState(newValue = colorScheme.onBackground)
}

@Composable
fun Material3Theme.primaryColorAsState(): State<Color> {
    return rememberUpdatedState(newValue = colorScheme.primary)
}

@Composable
fun Material3Theme.surfaceColorAsState(): State<Color> {
    return rememberUpdatedState(newValue = colorScheme.surface)
}

@Composable
fun Material3Theme.surfaceContentColorAsState(): State<Color> {
    return rememberUpdatedState(newValue = colorScheme.onSurface)
}

@Composable
fun Material3Theme.surfaceVariantColorAsState(): State<Color> {
    return rememberUpdatedState(newValue = colorScheme.surfaceVariant)
}

@Composable
fun Material3Theme.surfaceVariantContentColorAsState(): State<Color> {
    return rememberUpdatedState(newValue = colorScheme.onSurfaceVariant)
}

@Composable
fun Material3Theme.secondaryContainerColorAsState(): State<Color> {
    return rememberUpdatedState(newValue = colorScheme.secondaryContainer)
}

@Composable
fun Material3Theme.secondaryContainerContentColorAsState(): State<Color> {
    return rememberUpdatedState(newValue = colorScheme.onSecondaryContainer)
}

@Composable
fun Material3Theme.surfaceColorAsState(
    transform: (Color) -> Color
): State<Color> {
    return rememberUpdatedState(newValue = transform(colorScheme.surface))
}

@Composable
fun Material3Theme.outlineVariantColorAsState(): State<Color> {
    return rememberUpdatedState(newValue = colorScheme.outlineVariant)
}

@Composable
inline fun <T> Material3Theme.foldLightOrDarkTheme(
    light: () -> T,
    dark: () -> T
): T = foldLightOrDarkTheme(!LocalIsThemeDark.current, light, dark)

inline fun <T> Material3Theme.foldLightOrDarkTheme(
    isLight: Boolean,
    light: () -> T,
    dark: () -> T
): T = if (isLight) light() else dark()

@Composable
inline fun Material3Theme.blackOrWhite(): Color = if (LocalIsThemeDark.current) {
    Color.Black
} else {
    Color.White
}

@Composable
inline fun Material3Theme.blackOrWhiteContent(): Color = if (LocalIsThemeDark.current) {
    Color.White
} else {
    Color.Black
}