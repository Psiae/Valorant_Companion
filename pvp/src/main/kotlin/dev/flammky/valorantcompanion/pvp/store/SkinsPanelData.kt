package dev.flammky.valorantcompanion.pvp.store

import dev.flammky.valorantcompanion.base.time.ISO8601
import dev.flammky.valorantcompanion.pvp.store.currency.StoreCost
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlin.time.Duration

data class SkinsPanelData(
    val open: Boolean,
    val offer: Result<Offer?>
) {

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
        val reward: Reward
    )

    data class Reward(
        val itemType: ItemType,
        val itemID: String,
        val quantity: Long
    )
}
