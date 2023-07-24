package dev.flammky.valorantcompanion.live.loadout.presentation.root

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.flammky.valorantcompanion.live.main.LiveMainScreenContainer

@Composable
internal fun LiveLoadout(
    modifier: Modifier = Modifier,
    openScreen: (@Composable LiveMainScreenContainer.() -> Unit) -> Unit
) {
    LiveLoadoutPlacement(
        modifier = modifier,
        surface = { LiveLoadoutSurface() },
        content = {}
    )
}

@Composable
private fun LiveLoadoutPlacement(
    modifier: Modifier,
    surface: @Composable () -> Unit,
    content: @Composable () -> Unit
) = Box(modifier = modifier.fillMaxSize()) {
    surface()
    content()
}