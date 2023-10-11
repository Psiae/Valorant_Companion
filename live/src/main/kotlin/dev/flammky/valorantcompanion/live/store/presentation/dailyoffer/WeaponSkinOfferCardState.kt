package dev.flammky.valorantcompanion.live.store.presentation.dailyoffer

import dev.flammky.valorantcompanion.assets.LocalImage
import dev.flammky.valorantcompanion.base.UNSET
import dev.flammky.valorantcompanion.pvp.store.weapon.skin.WeaponSkinTier

data class WeaponSkinOfferCardState(
    val tier: WeaponSkinTier,
    val tierImageKey: Any,
    val tierImage: LocalImage<*>,
    val displayImageKey: Any,
    val displayImage: LocalImage<*>,
    val displayName: String,
    val costText: String,
    val costImageKey: Any,
    val costImage: LocalImage<*>,
    val showLoading: Boolean,
    val requireRefresh: Boolean,
    val refresh: () -> Unit
) : UNSET<WeaponSkinOfferCardState> by Companion {

    override fun equals(other: Any?): Boolean {
        if (other === this) return true
        if (other !is WeaponSkinOfferCardState) return false

        other as WeaponSkinOfferCardState

        return other.tier == tier && other.tierImageKey == tierImageKey && other.displayImageKey == displayImageKey &&
                other.displayName == displayName && other.costText == costText && other.costImageKey == costImageKey && other.showLoading == showLoading && other.requireRefresh == requireRefresh && other.refresh == refresh
    }

    override fun hashCode(): Int {
        var result = 0
        result += tier.hashCode()
        result *= 31 ; result += tierImageKey.hashCode()
        result *= 31 ; result += displayImageKey.hashCode()
        result *= 31 ; result += displayName.hashCode()
        result *= 31 ; result += costText.hashCode()
        result *= 31 ; result += costImageKey.hashCode()
        result *= 31 ; result += showLoading.hashCode()
        result *= 31 ; result += requireRefresh.hashCode()
        result *= 31 ; result += refresh.hashCode()
        return result
    }

    override fun toString(): String {
        return "WeaponSkinOfferCardState(tier=$tier, tierImageKey=$tierImageKey, displayImageKey=$displayImageKey, displayName=$displayName, costText=$costText, costImageKey=$costImageKey, requireRefresh=$requireRefresh, refresh=$refresh)"
    }

    companion object : UNSET<WeaponSkinOfferCardState> {

        override val UNSET: WeaponSkinOfferCardState = WeaponSkinOfferCardState(
            tier = WeaponSkinTier.UNSET,
            tierImageKey = this,
            tierImage = LocalImage.None,
            displayImageKey = this,
            displayImage = LocalImage.None,
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