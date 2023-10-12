package dev.flammky.valorantcompanion.pvp.store

import dev.flammky.valorantcompanion.base.UNSET
import dev.flammky.valorantcompanion.base.time.ISO8601
import dev.flammky.valorantcompanion.pvp.store.currency.StoreCost
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlin.time.Duration

data class SkinsPanelStore(
    val open: Boolean,
    val offer: Result<Offer?>
): UNSET<SkinsPanelStore> by Companion {

    data class Offer(
        val offeredItemIds: ImmutableList<String>,
        val itemOffers: ImmutableMap<String, ItemOffer>,
        val remainingDuration: Duration
    )

    data class ItemOffer(
        val offerId: String,
        val isDirectPurchase: Boolean,
        val startDate: ISO8601,
        val cost: StoreCost,
        val rewards: ImmutableList<Reward>
    )

    data class Reward(
        val itemType: ItemType,
        val itemID: String,
        val quantity: Long
    )

    companion object : UNSET<SkinsPanelStore> {

        override val UNSET: SkinsPanelStore = SkinsPanelStore(
            open = false,
            offer = Result.failure(Exception("UNSET"))
        )
    }
}
