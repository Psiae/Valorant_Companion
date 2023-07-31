package dev.flammky.valorantcompanion.assets.spray

interface ValorantSprayAssetEndpoint {

    fun buildImageUrl(
        uuid: String,
        type: ValorantSprayImageType
    ): String
}