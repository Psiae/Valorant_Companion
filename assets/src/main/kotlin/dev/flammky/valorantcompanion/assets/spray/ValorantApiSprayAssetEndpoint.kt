package dev.flammky.valorantcompanion.assets.spray

class ValorantApiSprayAssetEndpoint(): ValorantSprayAssetEndpoint {

    override fun buildImageUrl(uuid: String, type: ValorantSprayImageType): String {
        val base = MEDIA_SPRAY_URL
        val typeString = when(type) {
            ValorantSprayImageType.DISPLAY_ICON -> "displayicon"
            is ValorantSprayImageType.FULL_ICON -> {
                if (type.transparentBackground) "fulltransparenticon" else "fullicon"
            }
        }
        val fileFormatExtension = "png"
        return "$base$uuid/$typeString.$fileFormatExtension"
    }

    override fun buildIdentityUrl(uuid: String): String {
        val base = SPRAYS_URL
        return "$base$uuid"
    }

    override fun buildSprayLevelDisplayImageUrl(uuid: String): String {
        val base = MEDIA_SPRAYLEVELS_URL
        return "$base$uuid"
    }

    companion object {
        const val BASE_URL = "https://valorant-api.com/v1/"
        const val SPRAYS_URL = BASE_URL + "sprays/"
        const val BASE_MEDIA_URL = "https://media.valorant-api.com/"
        const val MEDIA_SPRAY_URL = BASE_MEDIA_URL + "sprays/"
        const val MEDIA_SPRAYLEVELS_URL = BASE_MEDIA_URL + "spraylevels/"
    }
}