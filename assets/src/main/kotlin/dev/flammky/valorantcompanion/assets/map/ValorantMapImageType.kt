package dev.flammky.valorantcompanion.assets.map

sealed class ValorantMapImageType(
    val name: String
) {

    object MiniMap : ValorantMapImageType("minimap") {

    }

    object ListView : ValorantMapImageType("listview") {

    }

    object Splash : ValorantMapImageType("splash") {

    }
}
