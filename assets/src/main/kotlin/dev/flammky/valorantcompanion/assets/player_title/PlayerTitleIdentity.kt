package dev.flammky.valorantcompanion.assets.player_title

data class PlayerTitleIdentity(
    val uuid: String,
    val description: String,
    val titleText: String,
    val hiddenIfNotOwned: Boolean
)
