package dev.flammky.valorantcompanion.assets.weapon.gunbuddy

sealed class GunBuddyImageType(
    val name: String
) {

    object NONE : GunBuddyImageType("none")

    object DISPLAY_ICON : GunBuddyImageType("displayIcon")
}
