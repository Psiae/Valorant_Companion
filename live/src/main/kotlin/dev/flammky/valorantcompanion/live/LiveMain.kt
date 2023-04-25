package dev.flammky.valorantcompanion.live

import androidx.compose.runtime.Composable
import dev.flammky.valorantcompanion.live.party.presentation.PartyColumn
import dev.flammky.valorantcompanion.live.party.presentation.rememberLivePartyPresenter

@Composable
fun LiveMain() {

    PartyColumn(
        state = rememberLivePartyPresenter().present()
    )
}