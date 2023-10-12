package dev.flammky.valorantcompanion.assets.weapon.skin

interface WeaponSkinEndpoint {

    val ID: String

    fun buildIdentityUrl(id: String): String

    fun buildImageUrl(id: String, type: WeaponSkinImageType): String
}