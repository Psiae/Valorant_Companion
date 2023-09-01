package dev.flammky.valorantcompanion.live.store.presentation.root

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import dev.flammky.valorantcompanion.base.theme.material3.Material3Theme
import dev.flammky.valorantcompanion.base.theme.material3.backgroundColorAsState
import dev.flammky.valorantcompanion.base.theme.material3.localMaterial3Surface

@Composable
fun LiveStoreSurface(
    modifier: Modifier
) = Box(
    modifier = modifier
        .fillMaxSize()
        .localMaterial3Surface()
)