package dev.flammky.valorantcompanion.live.ingame

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.flammky.valorantcompanion.base.theme.material3.Material3Theme
import dev.flammky.valorantcompanion.base.theme.material3.backgroundColorAsState

@Composable
internal fun LiveInGameBackground(modifier: Modifier) = Box(
    modifier
        .fillMaxSize()
        .background(Material3Theme.backgroundColorAsState().value)
)