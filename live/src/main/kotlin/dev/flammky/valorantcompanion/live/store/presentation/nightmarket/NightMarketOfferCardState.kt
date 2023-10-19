package dev.flammky.valorantcompanion.live.store.presentation.nightmarket

import dev.flammky.valorantcompanion.assets.LocalImage
import dev.flammky.valorantcompanion.base.UNSET
import dev.flammky.valorantcompanion.pvp.store.weapon.skin.WeaponSkinTier

data class NightMarketOfferCardState(
    val tier: WeaponSkinTier,
    val tierImageKey: Any,
    val tierImage: LocalImage<*>,
    val displayImageKey: Any,
    val displayImage: LocalImage<*>,
    val displayName: String,
    val discountPercentageText: String,
    val discountedAmountText: String,
    val costText: String,
    val costImageKey: Any,
    val costImage: LocalImage<*>,
    val showLoading: Boolean,
    val requireRefresh: Boolean,
    val refresh: () -> Unit
): UNSET<NightMarketOfferCardState> by Companion {

    override fun equals(other: Any?): Boolean {
        if (other === this) return true
        if (other !is NightMarketOfferCardState) return false

        other as NightMarketOfferCardState

        return other.tier == tier && other.tierImageKey == tierImageKey && other.displayImageKey == displayImageKey &&
                other.displayName == displayName && other.discountPercentageText == discountPercentageText &&
                other.discountedAmountText == discountedAmountText &&  other.costText == costText &&
                other.costImageKey == costImageKey && other.showLoading == showLoading &&
                other.requireRefresh == requireRefresh && other.refresh == refresh
    }

    override fun hashCode(): Int {
        var result = 0
        result += tier.hashCode()
        result *= 31 ; result += tierImageKey.hashCode()
        result *= 31 ; result += displayImageKey.hashCode()
        result *= 31 ; result += displayName.hashCode()
        result *= 31 ; result += discountPercentageText.hashCode()
        result *= 31 ; result += discountedAmountText.hashCode()
        result *= 31 ; result += costText.hashCode()
        result *= 31 ; result += costImageKey.hashCode()
        result *= 31 ; result += showLoading.hashCode()
        result *= 31 ; result += requireRefresh.hashCode()
        result *= 31 ; result += refresh.hashCode()
        return result
    }

    override fun toString(): String {
        return "NightMarketOfferCardState(" +
                "tier=$tier," +
                "tierImageKey=$tierImageKey," +
                "displayImageKey=$displayImageKey," +
                "displayName=$displayName," +
                "discountPercentageText=$discountPercentageText," +
                "costText=$costText," +
                "costImageKey=$costImageKey," +
                "showLoading=$showLoading," +
                "requireRefresh=$requireRefresh," +
                "refresh=$refresh" +
                ")"
    }

    companion object : UNSET<NightMarketOfferCardState> {

        override val UNSET: NightMarketOfferCardState = NightMarketOfferCardState(
            tier = WeaponSkinTier.UNSET,
            tierImageKey = this,
            tierImage = LocalImage.None,
            displayImageKey = this,
            displayImage = LocalImage.None,
            discountPercentageText = "",
            discountedAmountText = "",
            displayName = "",
            costText = "",
            costImageKey = this,
            costImage = LocalImage.None,
            showLoading = false,
            requireRefresh = false,
            refresh = {},
        )
    }
}