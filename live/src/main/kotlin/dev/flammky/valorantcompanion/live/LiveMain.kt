package dev.flammky.valorantcompanion.live

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import dev.flammky.valorantcompanion.live.party.presentation.LiveParty

@Composable
fun LiveMain() {
    LiveMainPlacement(
        liveParty = { LiveParty(modifier = Modifier) }
    )
}

@Composable
private fun LiveMainPlacement(
    liveParty: @Composable () -> Unit
) {
    Column {
        // TODO: Don't consume here
        Spacer(modifier = Modifier.height(
            with(LocalDensity.current) { WindowInsets.statusBars.getTop(this).toDp() }
        ))
        liveParty()
    }
}