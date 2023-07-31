package dev.flammky.valorantcompanion.live.loadout.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.flammky.valorantcompanion.base.debug.debugBlock
import dev.flammky.valorantcompanion.pvp.PvpConstants
import dev.flammky.valorantcompanion.pvp.spray.C
import kotlinx.collections.immutable.persistentListOf

@Composable
fun SprayLoadoutScreenContent(
    modifier: Modifier,
    state: SprayLoadoutScreenState
) {

    SprayLoadoutScreenContentPlacement(
        modifier = modifier,
        sprayPicker = { sprayPickerModifier ->
            SprayLoadoutPicker(
                modifier = sprayPickerModifier,
                state = rememberSprayLoadoutPickerPresenter().present()
            )
        },
        sprayPickerPool = { sprayPoolModifier ->
            SprayLoadoutPickerPool(
                modifier = sprayPoolModifier,
                ownedSprays = debugBlock {
                    persistentListOf<String>().builder()
                        .apply {
                            repeat(17) { index ->
                                add(index.toString())
                            }
                        }
                        .build()
                }
            )
        }
    )
}

@Composable
private fun SprayLoadoutScreenContentPlacement(
    modifier: Modifier,
    sprayPicker: @Composable (Modifier) -> Unit,
    sprayPickerPool: @Composable (Modifier) -> Unit
) {
    Column(modifier.fillMaxSize()) {
        sprayPicker(Modifier.padding(12.dp))
        Spacer(Modifier.height(12.dp))
        sprayPickerPool(Modifier.padding(12.dp))
    }
}