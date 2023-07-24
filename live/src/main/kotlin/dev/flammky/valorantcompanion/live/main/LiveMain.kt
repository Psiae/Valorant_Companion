package dev.flammky.valorantcompanion.live.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun LiveMain(

) = LiveMainPlacement(
    surface = { LiveMainSurface() },
    content = { LiveMainContent() }
)

@Composable
internal inline fun LiveMainPlacement(
    surface: @Composable () -> Unit,
    content: @Composable () -> Unit
) = Box(modifier = Modifier.fillMaxSize()) {
    surface()
    content()
}