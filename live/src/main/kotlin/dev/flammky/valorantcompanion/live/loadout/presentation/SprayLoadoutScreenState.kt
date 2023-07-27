package dev.flammky.valorantcompanion.live.loadout.presentation

import kotlinx.collections.immutable.ImmutableList

data class SprayLoadoutScreenState(
    val loadoutData: LoadoutData
) {


    data class LoadoutData(
        val activeSprays: ImmutableList<String>,
        val ownedSprays: ImmutableList<String>
    )
}