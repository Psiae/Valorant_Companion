package dev.flammky.valorantcompanion.pvp.loadout

data class IdentityLoadout(
    val playerCardId: String,
    val playerTitleId: String,
    val accountLevel: Int,
    val preferredLevelBorderId: String,
    val hideAccountLevel: Boolean
)
