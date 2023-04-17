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