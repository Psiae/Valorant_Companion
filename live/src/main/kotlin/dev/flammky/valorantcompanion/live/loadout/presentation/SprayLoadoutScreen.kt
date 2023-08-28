package dev.flammky.valorantcompanion.live.loadout.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.tooling.preview.Preview
import dev.flammky.valorantcompanion.base.theme.material3.DefaultMaterial3Theme
import dev.flammky.valorantcompanion.base.theme.material3.localMaterial3Background
import dev.flammky.valorantcompanion.base.theme.material3.localMaterial3Surface
import kotlinx.collections.immutable.persistentListOf

@Composable
internal fun SprayLoadoutScreen(
    modifier: Modifier,
    state: SprayLoadoutScreenState,
    dismiss: () -> Unit
) {
    SprayLoadoutScreenPlacement(
        modifier = modifier,
        surface = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .localMaterial3Surface()
            )
        },
        content = {
            SprayLoadoutScreenContent(
                modifier = Modifier,
                state = state
            )
        }
    )
}

@Composable
private fun SprayLoadoutScreenPlacement(
    modifier: Modifier,
    surface: @Composable () -> Unit,
    content: @Composable () -> Unit,
) = Box(modifier = modifier) {
    surface()
    content()
}
