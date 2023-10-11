package dev.flammky.valorantcompanion.assets.bundle

sealed class BundleImageType(
    open val name: String
) {

    object DISPLAY : BundleImageType("display")

    object DISPLAY_VERTICAL : BundleImageType("display_vertical")

    object NONE : BundleImageType("none")
}
