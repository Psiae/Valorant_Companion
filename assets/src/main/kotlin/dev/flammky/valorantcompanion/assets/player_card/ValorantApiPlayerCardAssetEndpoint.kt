package dev.flammky.valorantcompanion.assets.player_card

import dev.flammky.valorantcompanion.assets.PlayerCardArtType

class ValorantApiPlayerCardAssetEndpoint : ValorantPlayerCardAssetEndpoint {

    override fun buildArtUrl(id: String, type: PlayerCardArtType): String {
        val typeStr = when (type) {
            PlayerCardArtType.SMALL -> "smallart"
            PlayerCardArtType.TALL -> "largeart"
            PlayerCardArtType.WIDE -> "wideart"
        }
        val ext = "png"
        return "https://media.valorant-api.com/playercards/$id/$typeStr.$ext"
    }
}