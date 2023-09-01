package dev.flammky.valorantcompanion.pvp.store

import dev.flammky.valorantcompanion.base.time.ISO8601
import dev.flammky.valorantcompanion.pvp.store.currency.StoreCost
import kotlinx.collections.immutable.ImmutableList
import kotlin.time.Duration

data class AccessoryStoreData(
    val open: Boolean,
    val offer: Result<Offer?>,
) {

    data class Offer(
        val storeFrontId: String,
        val offers: ImmutableList<ItemOffer>,
        val remainingDuration: Duration
    )

    data class ItemOffer(
        val id: String,
        val isDirectPurchase: Boolean,
        val startDate: ISO8601,
        val cost: StoreCost,
        val reward: Reward,
        val contractID: String
    )

    data class Reward(
        val itemType: ItemType,
        val itemID: String,
        val quantity: Long
    )
}