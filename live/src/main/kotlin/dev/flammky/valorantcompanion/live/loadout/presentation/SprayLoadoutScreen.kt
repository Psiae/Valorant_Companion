package dev.flammky.valorantcompanion.live.loadout.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.tooling.preview.Preview
import dev.flammky.valorantcompanion.base.theme.material3.DefaultMaterial3Theme
import dev.flammky.valorantcompanion.base.theme.material3.localMaterial3Background
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
                    .pointerInput(Unit) {}
                    .localMaterial3Background()
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
    content: @Composable () -> Unit
) = Box(modifier = modifier) {
    surface()
    content()
}


@Composable
@Preview
internal fun SprayLoadoutScreenPreview() {
    DefaultMaterial3Theme(dark = true) {
        SprayLoadoutScreen(
            modifier = Modifier,
            state = SprayLoadoutScreenState(
                loadoutData = SprayLoadoutScreenState.LoadoutData(
                    activeSprays = persistentListOf("a", "b", "c"),
                    ownedSprays = persistentListOf("a", "b", "c", "d")
                )
            ),
            dismiss = {}
        )
    }
}
