package dev.flammky.valorantcompanion.pvp.store

import dev.flammky.valorantcompanion.base.time.ISO8601
import dev.flammky.valorantcompanion.pvp.store.currency.StoreCost
import kotlinx.collections.immutable.ImmutableList

data class UpgradeCurrencyStoreData(
    val open: Boolean,
    val offers: Result<Offer?>,
) {

    data class Offer(
        val offers: ImmutableList<ItemOffer>?
    )

    data class ItemOffer(
        val id: String,
        val storeFrontItemID: String,
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