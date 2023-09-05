package dev.flammky.valorantcompanion.live.store.presentation.root

import dev.flammky.valorantcompanion.base.UNSET

internal data class LiveStoreState(
    val dailyOfferEnabled: Boolean,
    val nightMarketEnabled: Boolean,
    val agentsEnabled: Boolean
): UNSET<LiveStoreState> by Companion {

    companion object : UNSET<LiveStoreState> {

        override val UNSET: LiveStoreState = LiveStoreState(
            dailyOfferEnabled = false,
            nightMarketEnabled = false,
            agentsEnabled = false
        )
    }
}
