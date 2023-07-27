package dev.flammky.valorantcompanion.live.loadout.presentation

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.lazy.layout.LazyLayout
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
internal fun UserSprayLoadouts(
    modifier: Modifier,
    ownedSprayCount: Int,
    getOwnedSpray: (Int) -> String
) = BoxWithConstraints(
    modifier = modifier
) {
}




private val SprayLoadoutCellMinSize = 50.dp