package dev.flammky.valorantcompanion.assets.map

interface ValorantMapAssetEndpoint {

    fun buildImageUrl(
        id: String,
        type: ValorantMapImageType
    ): String
}