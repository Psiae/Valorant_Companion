package dev.flammky.valorantcompanion.assets.spray

sealed class ValorantSprayImageType(
    val name: String
) {

    data class FULL_ICON(
        val transparentBackground: Boolean
    ) : ValorantSprayImageType(
        name = when {
            transparentBackground -> "fullTransparentIcon"
            else -> "fullIcon"
        }
    )

    object DISPLAY_ICON : ValorantSprayImageType("displayIcon")
}
