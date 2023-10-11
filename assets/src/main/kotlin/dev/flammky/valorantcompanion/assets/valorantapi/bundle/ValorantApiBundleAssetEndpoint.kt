package dev.flammky.valorantcompanion.assets.valorantapi.bundle

import dev.flammky.valorantcompanion.assets.bundle.BundleImageType
import dev.flammky.valorantcompanion.assets.bundle.ValorantBundleAssetEndpoint

class ValorantApiBundleAssetEndpoint : ValorantBundleAssetEndpoint {


    override fun buildImageUrl(uuid: String, type: BundleImageType): String {
        val ext = "png"
        return "$BUNDLE_MEDIA_URL/$uuid/${imageTypeUrlSegment(type)}.$ext"
    }

    companion object {
        const val BASE_MEDIA_URL = "https://media.valorant-api.com"
        const val BUNDLE_MEDIA_URL = "$BASE_MEDIA_URL/bundles"

        fun imageTypeUrlSegment(type: BundleImageType) = when (type) {
            BundleImageType.DISPLAY -> "displayicon"
            BundleImageType.DISPLAY_VERTICAL -> "verticalpromoimage"
            BundleImageType.NONE -> error("Cannot build \'NONE\' BundleImageType url")
        }
    }
}