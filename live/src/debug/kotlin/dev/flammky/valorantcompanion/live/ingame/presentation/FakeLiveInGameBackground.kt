package dev.flammky.valorantcompanion.live.ingame.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import dev.flammky.valorantcompanion.base.theme.material3.Material3Theme
import dev.flammky.valorantcompanion.base.theme.material3.backgroundColorAsState
import dev.flammky.valorantcompanion.assets.R as R_ASSET

@Composable
internal fun FakeLiveInGameBackground(modifier: Modifier) {
    AsyncImage(
        modifier = modifier.fillMaxSize().background(Material3Theme.backgroundColorAsState().value),
        model = /* TODO: */ null,
        contentDescription = null,
        contentScale = ContentScale.Crop
    )
}