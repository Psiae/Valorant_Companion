package dev.flammky.valorantcompanion.pvp.store

import dev.flammky.valorantcompanion.base.UNSET
import dev.flammky.valorantcompanion.base.time.ISO8601
import dev.flammky.valorantcompanion.pvp.store.currency.StoreCost
import kotlinx.collections.immutable.ImmutableList
import kotlin.time.Duration

data class BonusStore(
    val open: Boolean,
    val offer: Result<Offer?>
): UNSET<BonusStore> by Companion {

    data class Offer(
        val offerID: String,
        val offers: ImmutableList<ItemOffer>,
        val remainingDuration: Duration
    )

    data class ItemOffer(
        val offerID: String,
        val isDirectPurchase: Boolean,
        val startDate: ISO8601,
        val cost: StoreCost,
        val discountPercent: Float,
        val discountCost: StoreCost,
        val reward: ItemOfferReward,
        val isSeen: Boolean
    )

    data class ItemOfferReward(
        val itemType: ItemType,
        val itemID: String,
        val quantity: Long
    )

    companion object : UNSET<BonusStore> {

        override val UNSET: BonusStore = BonusStore(
            open = false,
            offer = Result.failure(Exception("UNSET"))
        )
    }
}
