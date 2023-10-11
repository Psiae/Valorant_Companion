package dev.flammky.valorantcompanion.live.store.presentation.dailyoffer

import dev.flammky.valorantcompanion.base.UNSET
import dev.flammky.valorantcompanion.pvp.store.AccessoryStore
import dev.flammky.valorantcompanion.pvp.store.FeaturedBundleStore
import dev.flammky.valorantcompanion.pvp.store.SkinsPanelStore

data class DailyOfferScreenState(
    val featuredBundle: FeaturedBundleStore,
    val skinsPanel: SkinsPanelStore,
    val accessory: AccessoryStore,
    val needManualRefresh: Boolean,
    val needManualRefreshMessage: String,
    val manualRefresh: () -> Unit
): UNSET<DailyOfferScreenState> by Companion {

    companion object : UNSET<DailyOfferScreenState> {


        override val UNSET: DailyOfferScreenState = DailyOfferScreenState(
            FeaturedBundleStore.UNSET,
            SkinsPanelStore.UNSET,
            AccessoryStore.UNSET,
            needManualRefresh = false,
            needManualRefreshMessage = "",
            manualRefresh = {}
        )
    }

}