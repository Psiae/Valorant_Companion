package dev.flammky.valorantcompanion.assets.player_card

interface ValorantPlayerCardAssetEndpoint {

    fun buildArtUrl(
        id: String,
        type: PlayerCardArtType
    ): String
}