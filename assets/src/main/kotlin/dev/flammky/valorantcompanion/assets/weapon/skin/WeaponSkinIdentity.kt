package dev.flammky.valorantcompanion.assets.weapon.skin

import dev.flammky.valorantcompanion.pvp.store.weapon.skin.WeaponSkinTier

data class WeaponSkinIdentity(
    val uuid: String,
    val displayName: String,
    val tier: WeaponSkinTier
)
