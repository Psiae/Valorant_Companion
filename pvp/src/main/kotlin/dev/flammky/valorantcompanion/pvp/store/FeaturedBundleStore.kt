package dev.flammky.valorantcompanion.pvp.store

import dev.flammky.valorantcompanion.base.UNSET
import dev.flammky.valorantcompanion.base.time.ISO8601
import dev.flammky.valorantcompanion.pvp.store.currency.StoreCost
import kotlinx.collections.immutable.ImmutableList
import kotlin.time.Duration

data class FeaturedBundleStore(
    val open: Boolean,
    val offer: Result<Offer?>
): UNSET<FeaturedBundleStore> by Companion {

    data class Offer(
        val bundle: Bundle,
        val bundles: ImmutableList<Bundle>,
        val bundleRemainingDuration: Duration
    )

    data class Bundle(
        val id: String,
        val dataAssetID: String,
        val currencyID: String,
        val itemOffers: ImmutableList<ItemBaseOffer>,
        val itemDiscountedOffers: ImmutableList<ItemDiscountedOffer>?,
        val totalBaseCost: StoreCost?,
        val totalDiscountedCost: StoreCost?,
        val totalDiscountPercent: Float,
        val durationRemaining: Duration,
        val wholesaleOnly: Boolean
    )

    data class ItemBaseOffer(
        val baseCost: StoreCost,
        val discountPercent: Float,
        val discountedPrice: Long,
        val isPromoItem: Boolean,
        val reward: Reward
    )

    data class ItemDiscountedOffer(
        val offerID: String,
        val isDirectPurchase: Boolean,
        val startDate: ISO8601,
        val baseCost: StoreCost,
        val discountPercent: Float,
        val discountedCost: StoreCost,
        val reward: Reward,

    )

    data class Reward(
        val itemType: ItemType,
        val itemID: String,
        val quantity: Long
    )

    companion object : UNSET<FeaturedBundleStore> {

        override val UNSET: FeaturedBundleStore = FeaturedBundleStore(
            open = false,
            offer = Result.failure(Exception("UNSET"))
        )
    }
}