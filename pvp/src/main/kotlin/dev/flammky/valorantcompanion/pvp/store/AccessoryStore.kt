package dev.flammky.valorantcompanion.pvp.store

import dev.flammky.valorantcompanion.base.UNSET
import dev.flammky.valorantcompanion.base.time.ISO8601
import dev.flammky.valorantcompanion.pvp.store.currency.StoreCost
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlin.time.Duration

data class AccessoryStore(
    val open: Boolean,
    val offer: Result<Offer?>,
): UNSET<AccessoryStore> by Companion {

    data class Offer(
        val storeFrontId: String,
        val offers: ImmutableMap<String, ItemOffer>,
        val remainingDuration: Duration
    )

    data class ItemOffer(
        val id: String,
        val isDirectPurchase: Boolean,
        val startDate: ISO8601,
        val cost: StoreCost,
        val rewards: ImmutableList<Reward>,
        val contractID: String
    )

    data class Reward(
        val itemType: ItemType,
        val itemID: String,
        val quantity: Long
    )

    companion object : UNSET<AccessoryStore> {

        override val UNSET: AccessoryStore = AccessoryStore(
            open = false,
            offer = Result.failure(Exception("UNSET"))
        )
    }
}