package dev.flammky.valorantcompanion.live.store.presentation.dailyoffer

import dev.flammky.valorantcompanion.assets.LocalImage
import dev.flammky.valorantcompanion.base.UNSET
import kotlin.time.Duration

data class AccessoryOfferPanelState(
    val currencyCount: Int,
    val getCurrencyImageKey: (Int) -> Any,
    val getCurrencyImage: (Int) -> LocalImage<*>,
    val offerCount: Int,
    val getOfferDisplayImageKey: (Int) -> Any,
    val getOfferDisplayImage: (Int) -> LocalImage<*>,
    val durationLeft: Duration,
): UNSET<AccessoryOfferPanelState> by Companion {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is AccessoryOfferPanelState) return false

        other as AccessoryOfferPanelState

        return other.currencyCount == currencyCount &&
                other.getCurrencyImageKey == getCurrencyImageKey &&
                other.getCurrencyImage == getCurrencyImage &&
                other.offerCount == offerCount && other.getOfferDisplayImageKey == getOfferDisplayImageKey &&
                other.getOfferDisplayImage == getOfferDisplayImage && other.durationLeft == durationLeft
    }

    override fun hashCode(): Int {
        var result = 0
        result = currencyCount.hashCode()
        result *= 31 ; result += getCurrencyImageKey.hashCode()
        result *= 31 ; result += getCurrencyImage.hashCode()
        result *= 31 ; result += offerCount.hashCode()
        result *= 31 ; result += getOfferDisplayImageKey.hashCode()
        result *= 31 ; result += getOfferDisplayImage.hashCode()
        result *= 31 ; result += durationLeft.hashCode()
        return result
    }

    override fun toString(): String {
        // TODO
        return super.toString()
    }

    companion object : UNSET<AccessoryOfferPanelState> {

        override val UNSET: AccessoryOfferPanelState = AccessoryOfferPanelState(
            currencyCount = 0,
            getCurrencyImageKey = {},
            getCurrencyImage = { LocalImage.None },
            offerCount = 0,
            getOfferDisplayImageKey = {},
            getOfferDisplayImage = { LocalImage.None },
            durationLeft = Duration.ZERO
        )
    }
}