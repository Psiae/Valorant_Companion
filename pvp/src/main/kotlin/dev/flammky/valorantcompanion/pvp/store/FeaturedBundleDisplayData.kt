package dev.flammky.valorantcompanion.pvp.store

data class FeaturedBundleDisplayData(
    val uuid: String,
    val displayName: String,
    val displayNameSubText: String?,
    val description: String,
    val extraDescription: String?,
    val useAdditionalContext: Boolean
)
