package dev.flammky.valorantcompanion.assets.player_title

abstract class PlayerTitleAssetEndpoint(
    val id: String
) {

    abstract suspend fun available(
        capability: Set<String>
    ): Boolean

    companion object {
        const val CAPABILITY_TITLE_IDENTITY = "CAPABILITY_TITLE_IDENTITY"
    }
}