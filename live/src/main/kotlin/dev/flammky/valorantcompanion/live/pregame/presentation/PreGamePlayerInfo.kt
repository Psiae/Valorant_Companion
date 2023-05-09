package dev.flammky.valorantcompanion.live.pregame.presentation

data class PreGamePlayerInfo(
    val puuid: String,
    val cardID: String,
    val titleID: String,
    val accountLevel: Int,
    val preferredLevelBorderID: String,
    val incognito: Boolean,
    val hideAccountLevel: Boolean
)
