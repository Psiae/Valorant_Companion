package dev.flammky.valorantcompanion.assets.weapon.skin

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap

class WeaponSkinsAssets(
    val version: String,
    val items: ImmutableMap<String, Item>
) {

    class Item(
        val uuid: String,
        val displayName: String,
        val contentTierUUID: String?,
        val gameAssetPath: String,
        val chromas: ImmutableMap<String, Chroma>,
        val levels: ImmutableMap<String, Level>
    ) {

        class Chroma(
            val uuid: String,
        )

        class Level(
            val uuid: String
        )
    }
}