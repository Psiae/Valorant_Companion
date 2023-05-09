package dev.flammky.valorantcompanion.assets.player_card

import dev.flammky.valorantcompanion.assets.PlayerCardArtType

interface ValorantPlayerCardAssetEndpoint {

    fun buildArtUrl(
        id: String,
        type: PlayerCardArtType
    ): String
}