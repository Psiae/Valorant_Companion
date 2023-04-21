package dev.flammky.valorantcompanion.assets

sealed class PlayerCardArtType(
    val widthPx: Int,
    val heightPx: Int,
    val name: String
) {
    object SMALL : PlayerCardArtType(
        128,
        128,
        "small"
    )
    object TALL : PlayerCardArtType(
        268,
        640,
        "tall"
    )
    object WIDE : PlayerCardArtType(
        452,
        128,
        "wide"
    )
}