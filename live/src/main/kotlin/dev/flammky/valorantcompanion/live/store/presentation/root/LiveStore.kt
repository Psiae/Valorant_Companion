package dev.flammky.valorantcompanion.live.store.presentation.root

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.flammky.valorantcompanion.live.main.LiveMainScreenContainer

@Composable
fun LiveStore(
    modifier: Modifier,
    openScreen: (@Composable LiveMainScreenContainer.() -> Unit) -> Unit
) {
    LiveStorePlacement(
        modifier = modifier,
        surface = { LiveStoreSurface(modifier = Modifier) },
        content = { /* TODO */ }
    )
}


@Composable
private fun LiveStorePlacement(
    modifier: Modifier,
    surface: @Composable () -> Unit,
    content: @Composable () -> Unit
) = Box(modifier.fillMaxSize()) {
    surface()
    content()
}