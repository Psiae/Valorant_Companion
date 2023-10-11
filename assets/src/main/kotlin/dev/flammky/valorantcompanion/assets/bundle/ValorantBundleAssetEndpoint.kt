package dev.flammky.valorantcompanion.assets.bundle

interface ValorantBundleAssetEndpoint {

    fun buildImageUrl(
        uuid: String,
        type: BundleImageType
    ): String
}
