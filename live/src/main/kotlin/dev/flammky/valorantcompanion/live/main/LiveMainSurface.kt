package dev.flammky.valorantcompanion.live.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import dev.flammky.valorantcompanion.base.theme.material3.Material3Theme
import dev.flammky.valorantcompanion.base.theme.material3.backgroundColorAsState

@Composable
internal fun LiveMainSurface() {
    Box(
        modifier = Modifier
            .pointerInput(Unit) {}
            .background(Material3Theme.backgroundColorAsState().value)
    )
}