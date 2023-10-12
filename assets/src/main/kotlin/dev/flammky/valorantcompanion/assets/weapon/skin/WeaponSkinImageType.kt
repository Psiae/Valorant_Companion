package dev.flammky.valorantcompanion.assets.weapon.skin

sealed class WeaponSkinImageType(
    val name: String
) {

    object DISPLAY_SMALL : WeaponSkinImageType("displayimage")

    object RENDER_FULL : WeaponSkinImageType("fullrender")

    object NONE : WeaponSkinImageType("none")
}
