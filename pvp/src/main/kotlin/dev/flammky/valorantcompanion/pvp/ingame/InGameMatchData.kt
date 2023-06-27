package dev.flammky.valorantcompanion.pvp.ingame

data class InGameMatchData(
    val matchID: String,
    val version: String,
    val mapID: String,
    val provisioningFlow: String,
    val gamePodID: String,
    val gameModeID: String,
    val queueID: String?,
    val players: List<InGamePlayer>,
    val isRanked: Boolean
)
