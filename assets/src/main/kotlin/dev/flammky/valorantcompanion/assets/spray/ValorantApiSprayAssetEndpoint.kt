package dev.flammky.valorantcompanion.assets.spray

class ValorantApiSprayAssetEndpoint(): ValorantSprayAssetEndpoint {

    override fun buildImageUrl(uuid: String, type: ValorantSprayImageType): String {
        val base = BASE_URL
        val typeString = when(type) {
            ValorantSprayImageType.DISPLAY_ICON -> "displayicon"
            is ValorantSprayImageType.FULL_ICON -> {
                if (type.transparentBackground) "fulltransparenticon" else "fullicon"
            }
        }
        val fileFormatExtension = "png"
        return "$base$uuid/$typeString.$fileFormatExtension"
    }

    companion object {
        const val BASE_URL = "https://media.valorant-api.com/sprays/"
    }
}