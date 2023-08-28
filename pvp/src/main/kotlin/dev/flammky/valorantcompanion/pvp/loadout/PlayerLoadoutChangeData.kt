package dev.flammky.valorantcompanion.pvp.loadout

import kotlinx.collections.immutable.ImmutableList

data class PlayerLoadoutChangeData(
    val guns: ImmutableList<GunLoadoutItem>,
    val sprays: ImmutableList<SprayLoadoutItem>,
    val identity: IdentityLoadout,
    val incognito: Boolean
) {}
