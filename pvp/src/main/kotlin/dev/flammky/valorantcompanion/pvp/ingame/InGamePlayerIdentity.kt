package dev.flammky.valorantcompanion.pvp.ingame

data class InGamePlayerIdentity(
    val puuid: String,
    val playerCardId: String,
    val playerTitleId: String,
    val accountLevel: Int,
    val preferredBorderId: String,
    val incognito: Boolean,
    val hideAccountLevel: Boolean
)