package dev.flammky.valorantcompanion.live

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import dev.flammky.valorantcompanion.live.match.presentation.root.LiveMatchUI
import dev.flammky.valorantcompanion.live.party.presentation.LivePartyUI

@Composable
fun LiveMain() {

    LiveMainPlacement(
        liveParty = {
            LivePartyUI(modifier = Modifier)
        },
        liveMatch = {
            LiveMatchUI(modifier = Modifier)
        }
    )
}

@Composable
private fun LiveMainPlacement(
    liveParty: @Composable () -> Unit,
    liveMatch: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .padding(
                top = with(LocalDensity.current) {
                    WindowInsets.statusBars.getTop(this).toDp()
                }
            )
    ) {
        liveParty()
        liveMatch()
    }
}