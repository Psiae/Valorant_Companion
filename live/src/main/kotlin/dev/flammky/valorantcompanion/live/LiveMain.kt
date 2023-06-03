package dev.flammky.valorantcompanion.live

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import dev.flammky.valorantcompanion.live.party.presentation.LivePartyUI

@Composable
fun LiveMain() = LiveMainPlacement(
    liveParty = { statusBarPadding ->
        LivePartyUI(modifier = Modifier.padding(top = statusBarPadding))
    }
)

@Composable
private fun LiveMainPlacement(
    liveParty: @Composable (statusBarPadding: Dp) -> Unit
) {
    Column {
        liveParty(
            statusBarPadding = with(LocalDensity.current) {
                WindowInsets.statusBars.getTop(this).toDp()
            }
        )
    }
}