package dev.flammky.valorantcompanion.live.store.presentation.root

import dev.flammky.valorantcompanion.base.UNSET
import dev.flammky.valorantcompanion.pvp.store.AccessoryStore
import dev.flammky.valorantcompanion.pvp.store.BonusStore
import dev.flammky.valorantcompanion.pvp.store.FeaturedBundleStore
import dev.flammky.valorantcompanion.pvp.store.SkinsPanelStore

internal data class LiveStoreState(
    val dailyOfferEnabled: Boolean,
    val nightMarketEnabled: Boolean,
    val agentsEnabled: Boolean,
    val featuredBundleStore: FeaturedBundleStore,
    val skinsPanelStore: SkinsPanelStore,
    val bonusStore: BonusStore,
    val accessoryStore: AccessoryStore
): UNSET<LiveStoreState> by Companion {

    companion object : UNSET<LiveStoreState> {

        override val UNSET: LiveStoreState = LiveStoreState(
            dailyOfferEnabled = false,
            nightMarketEnabled = false,
            agentsEnabled = false,
            featuredBundleStore = FeaturedBundleStore.UNSET,
            skinsPanelStore = SkinsPanelStore.UNSET,
            bonusStore = BonusStore.UNSET,
            accessoryStore = AccessoryStore.UNSET
        )
    }
}
