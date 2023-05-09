package dev.flammky.valorantcompanion.assets.map

class ValorantApiMapAssetEndpoint : ValorantMapAssetEndpoint {

    override fun buildImageUrl(id: String, type: ValorantMapImageType): String {
        val name = imageTypeName(type)
        return "https://media.valorant-api.com/maps/$id/$name.png"
    }

    private fun imageTypeName(type: ValorantMapImageType): String {
        return when(type) {
            ValorantMapImageType.MiniMap -> "displayicon"
            ValorantMapImageType.ListView -> "listviewicon"
            ValorantMapImageType.Splash -> "splash"
        }
    }
}