package dev.flammky.valorantcompanion.live.store.presentation.dailyoffer

import dev.flammky.valorantcompanion.assets.LocalImage
import dev.flammky.valorantcompanion.base.UNSET
import dev.flammky.valorantcompanion.pvp.store.currency.OtherStoreCurrency
import dev.flammky.valorantcompanion.pvp.store.currency.StoreCost
import dev.flammky.valorantcompanion.pvp.store.currency.StoreCurrency
import dev.flammky.valorantcompanion.pvp.store.weapon.skin.WeaponSkinTier
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentSetOf
import kotlin.time.Duration

data class FeaturedBundleDisplayState(
    val displayName: String,
    val durationLeft: Duration,
    // there's no guarantee that the offered items in the bundle is of the same tier
    val tiersKey: Any,
    val tiers: ImmutableSet<WeaponSkinTier>,
    val cost: StoreCost,
    val loadingImage: Boolean,
    val imageKey: Any,
    val image: LocalImage<*>,
    val error: Boolean,
    val errorMessage: String = ""
    /*
    * TODO:
    *  description
    *  extra description
    * */
): UNSET<FeaturedBundleDisplayState> by Companion {

    override fun equals(other: Any?): Boolean {
        if (other === this) return true
        if (other !is FeaturedBundleDisplayState) return false

        other as FeaturedBundleDisplayState

        return other.displayName == this.displayName && other.durationLeft == this.durationLeft &&
                other.tiersKey == this.tiersKey && other.cost == this.cost &&
                other.loadingImage == this.loadingImage && other.imageKey == this.imageKey &&
                other.error == this.error && other.errorMessage == this.errorMessage
    }

    override fun hashCode(): Int {
        var result = 0
        result += displayName.hashCode()
        result *= 31 ; result += durationLeft.hashCode()
        result *= 31 ; result += tiersKey.hashCode()
        result *= 31 ; result += cost.hashCode()
        result *= 31 ; result += loadingImage.hashCode()
        result *= 31 ; result += imageKey.hashCode()
        result *= 31 ; result += error.hashCode()
        result *= 31 ; result += errorMessage.hashCode()
        return result
    }

    companion object : UNSET<FeaturedBundleDisplayState> {

        override val UNSET: FeaturedBundleDisplayState = FeaturedBundleDisplayState(
            displayName = "",
            durationLeft = Duration.ZERO,
            tiersKey = Any(),
            tiers = persistentSetOf(),
            cost = StoreCost(OtherStoreCurrency("", "", ""), 0),
            loadingImage = false,
            imageKey = Any(),
            image = LocalImage.Resource(0),
            error = false,
            errorMessage = ""
        )
    }
}