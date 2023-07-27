package dev.flammky.valorantcompanion.assets.spray

sealed class SprayImageType(
    val name: String
) {

    data class FULL_ICON(
        val transparentBackground: Boolean
    ) : SprayImageType(
        name = when {
            transparentBackground -> "fullTransparentIcon"
            else -> "fullIcon"
        }
    )

    object DISPLAY_ICON : SprayImageType("displayIcon")
}
