package dev.flammky.valorantcompanion.live.store.presentation.dailyoffer.cpreview

import dev.flammky.valorantcompanion.base.UNSET
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlin.time.Duration

data class WeaponSkinOfferPanelState(
    val remainingDuration: Duration,
    val itemsKey: Any,
    val items: ImmutableList<WeaponSkinOfferPanelItem>
): UNSET<WeaponSkinOfferPanelState> by Companion {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is WeaponSkinOfferPanelState) return false

        other as WeaponSkinOfferPanelState

        return remainingDuration == other.remainingDuration && itemsKey == other.itemsKey
    }

    override fun hashCode(): Int {
        var result = 0
        result += remainingDuration.hashCode()
        result *= 31 ; result +=  itemsKey.hashCode()
        return result
    }

    companion object : UNSET<WeaponSkinOfferPanelState> {

        override val UNSET: WeaponSkinOfferPanelState = WeaponSkinOfferPanelState(
            remainingDuration = Duration.ZERO,
            itemsKey = Any(),
            items = persistentListOf()
        )
    }
}